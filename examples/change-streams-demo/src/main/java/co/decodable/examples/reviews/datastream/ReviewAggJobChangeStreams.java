/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream;

import co.decodable.examples.reviews.datastream.model.*;
import co.decodable.sdk.pipeline.DecodableDebeziumEnvelope.CDC_OP_TYPE;
import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;
import co.decodable.sdk.pipeline.serde.DecodableRecordDeserializationSchema;
import co.decodable.sdk.pipeline.serde.DecodableRecordSerializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;

@SourceStreams(ReviewAggJobChangeStreams.REVIEWS_CHANGE_STREAM)
@SinkStreams(ReviewAggJobChangeStreams.REVIEWS_CHANGE_STREAM_AGG)
public class ReviewAggJobChangeStreams {

  static final String REVIEWS_CHANGE_STREAM_AGG = "reviews_agg";
  static final String REVIEWS_CHANGE_STREAM = "reviews";

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DecodableStreamSource<ReviewRecord> source =
            DecodableStreamSource.<ReviewRecord>builder()
                    .withStreamName(REVIEWS_CHANGE_STREAM)
                    .withRecordDeserializationSchema(new DecodableRecordDeserializationSchema<>(ReviewRecord.class))
                    .build();

    DecodableStreamSink<ReviewAggRecord> sink =
            DecodableStreamSink.<ReviewAggRecord>builder()
                    .withStreamName(REVIEWS_CHANGE_STREAM_AGG)
                    .withRecordSerializationSchema(new DecodableRecordSerializationSchema<>(ReviewAggKey.class, ReviewAggValue.class))
                    .build();

    DataStream<ReviewAggRecord> stream =
            env.fromSource(source, WatermarkStrategy.noWatermarks(),
                            REVIEWS_CHANGE_STREAM)
                            .keyBy(review -> review.getValue().getAfter() != null
                                    ? review.getValue().getAfter().itemId
                                    :  review.getValue().getBefore().itemId)
                    .process(new ReviewsAggregatorFunction());


    stream.sinkTo(sink)
            .name(REVIEWS_CHANGE_STREAM_AGG);

    env.execute("Reviews Agg Processor");
  }

  public static class ReviewsAggregatorFunction extends KeyedProcessFunction<String, ReviewRecord, ReviewAggRecord> {

    ValueState<ReviewAggValue> itemIdAggState;

    @Override
    public void open(OpenContext openContext) throws Exception {
      super.open(openContext);
      ValueStateDescriptor<ReviewAggValue> itemIdAggStateDesc = new ValueStateDescriptor<>("itemIdAggState", ReviewAggValue.class);
      itemIdAggState = getRuntimeContext().getState(itemIdAggStateDesc);
    }

    @Override
    public void processElement(ReviewRecord record, KeyedProcessFunction<String, ReviewRecord, ReviewAggRecord>.Context ctx, Collector<ReviewAggRecord> out) throws Exception {
      ReviewAggRecord outputRecord;
      switch(CDC_OP_TYPE.valueOf(record.getValue().getOp().toUpperCase())) {
        case C:
        case R:
          outputRecord = handleCreateOrRead(ctx.getCurrentKey(), record);
          itemIdAggState.update(outputRecord.getValue().getAfter());
          out.collect(outputRecord);
          break;
        case U:
          outputRecord = handleUpdate(ctx.getCurrentKey(), record);
          itemIdAggState.update(outputRecord.getValue().getAfter());
          out.collect(outputRecord);
          break;
        case D:
          outputRecord = handleDelete(ctx.getCurrentKey(), record);
          if (outputRecord.getValue().getAfter() != null) {
            itemIdAggState.update(outputRecord.getValue().getAfter());
          } else {
            itemIdAggState.clear();
          }
          out.collect(outputRecord);
          break;
        default:
          throw new RuntimeException("unknown CDC operation type '" + record.getValue().getOp() + "'");
      }
    }

    private ReviewAggRecord handleCreateOrRead(String aggKey, ReviewRecord record) throws Exception {
      var before = itemIdAggState.value();
      if (before == null) {
        var reviewTexts = new HashMap<Long, String>();
        reviewTexts.put(record.getKey().id, record.getValue().getAfter().reviewText);
        var after = new ReviewAggValue(aggKey, 1, reviewTexts);
        return new ReviewAggRecord(
                new ReviewAggKey(aggKey),
                new ReviewAggEnvelope(null, after, CDC_OP_TYPE.C.name().toLowerCase(), record.getValue().getTs_ms())
        );
      } else {
        var reviewTexts = new HashMap<>(before.reviewTexts);
        reviewTexts.put(record.getKey().id, record.getValue().getAfter().reviewText);
        var after = new ReviewAggValue(aggKey, before.cnt + 1, reviewTexts);
        return new ReviewAggRecord(
                new ReviewAggKey(aggKey),
                new ReviewAggEnvelope(before, after, CDC_OP_TYPE.U.name().toLowerCase(), record.getValue().getTs_ms())
        );
      }
    }

    private ReviewAggRecord handleUpdate(String aggKey, ReviewRecord record) throws Exception {
      var before = itemIdAggState.value();
      if (before == null) {
        var reviewTexts = new HashMap<Long,String>();
        reviewTexts.put(record.getKey().id, record.getValue().getAfter().reviewText);
        var after = new ReviewAggValue(aggKey,1,reviewTexts);
        return new ReviewAggRecord(
                new ReviewAggKey(aggKey),
                new ReviewAggEnvelope(null,after,CDC_OP_TYPE.C.name().toLowerCase(),record.getValue().getTs_ms())
        );
      } else {
        var reviewTexts = new HashMap<>(before.reviewTexts);
        reviewTexts.put(record.getKey().id, record.getValue().getAfter().reviewText);
        var after = new ReviewAggValue(aggKey,before.cnt,reviewTexts);
        return new ReviewAggRecord(
                new ReviewAggKey(aggKey),
                new ReviewAggEnvelope(before,after,CDC_OP_TYPE.U.name().toLowerCase(),record.getValue().getTs_ms())
        );
      }
    }

    private ReviewAggRecord handleDelete(String aggKey, ReviewRecord record) throws Exception {
      var before = itemIdAggState.value();
      if (before == null) {
        var reviews = new HashMap<Long,String>();
        reviews.put(record.getKey().id, record.getValue().getBefore().reviewText);
        return new ReviewAggRecord(
                new ReviewAggKey(aggKey),
                new ReviewAggEnvelope(new ReviewAggValue(aggKey,1,reviews),null,CDC_OP_TYPE.D.name().toLowerCase(),record.getValue().getTs_ms())
        );
      } else {
        var reviews = new HashMap<>(before.reviewTexts);
        reviews.remove(record.getKey().id);
        if (reviews.isEmpty() || before.cnt-1 == 0) {
          return new ReviewAggRecord(
                  new ReviewAggKey(aggKey),
                  new ReviewAggEnvelope(before,null,CDC_OP_TYPE.D.name().toLowerCase(),record.getValue().getTs_ms())
          );
        }
        var after = new ReviewAggValue(aggKey,before.cnt-1,reviews);
        return new ReviewAggRecord(
                  new ReviewAggKey(aggKey),
                  new ReviewAggEnvelope(before,after,CDC_OP_TYPE.U.name().toLowerCase(),record.getValue().getTs_ms())
        );
      }
    }

  }

}

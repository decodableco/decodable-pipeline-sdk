/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream;

import co.decodable.examples.reviews.datastream.model.*;
import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;
import co.decodable.sdk.pipeline.serde.DecodableRecordDeserializationSchema;
import co.decodable.sdk.pipeline.serde.DecodableRecordSerializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

@SourceStreams(ReviewProcessorJobChangeStreams.REVIEWS_CHANGE_STREAM)
@SinkStreams(ReviewProcessorJobChangeStreams.REVIEWS_CHANGE_STREAM_PROCESSED)
public class ReviewProcessorJobChangeStreams {

  static final String REVIEWS_CHANGE_STREAM_PROCESSED = "reviews_processed";
  static final String REVIEWS_CHANGE_STREAM = "reviews";

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DecodableStreamSource<ReviewRecord> source =
            DecodableStreamSource.<ReviewRecord>builder()
                    .withStreamName(REVIEWS_CHANGE_STREAM)
                    .withRecordDeserializationSchema(new DecodableRecordDeserializationSchema<>(ReviewRecord.class))
                    .build();

    DecodableStreamSink<ReviewProcessedRecord> sink =
            DecodableStreamSink.<ReviewProcessedRecord>builder()
                    .withStreamName(REVIEWS_CHANGE_STREAM_PROCESSED)
                    .withRecordSerializationSchema(new DecodableRecordSerializationSchema<>(ReviewKey.class, ReviewProcessedValue.class))
                    .build();

    DataStream<ReviewProcessedRecord> stream =
            env.fromSource(source, WatermarkStrategy.noWatermarks(),
                            REVIEWS_CHANGE_STREAM)
                    .map(new ReviewLengthAdder());

    stream.sinkTo(sink)
            .name(REVIEWS_CHANGE_STREAM_PROCESSED);

    env.execute("Reviews Processor");
  }

  public static class ReviewLengthAdder extends RichMapFunction<ReviewRecord, ReviewProcessedRecord> {

    private static final long serialVersionUID = 1L;

    private Counter recordsProcessed;

    @Override
    public void open(Configuration parameters) throws Exception {
      recordsProcessed = getRuntimeContext()
              .getMetricGroup()
              .addGroup("DecodableMetrics")
              .counter("recordsProcessed", new SimpleCounter());
    }

    @Override
    public ReviewProcessedRecord map(ReviewRecord original) throws Exception {
      var envelope = original.getValue();
      var processed = new ReviewProcessedRecord(
              original.getKey(),
              new ReviewProcessedEnvelope(
                      createReviewProcessedValue(envelope.getBefore()),
                      createReviewProcessedValue(envelope.getAfter()),
                      envelope.getOp(),
                      envelope.getTs_ms()
              )
      );
      recordsProcessed.inc();
      return processed;
    }

    // NOTE:
    // This can only work for truly stateless transformations otherwise explicit
    // state per key handling is necessary to retrieve the before state accordingly.
    private static ReviewProcessedValue createReviewProcessedValue(ReviewValue original) {
      return original == null ? null : new ReviewProcessedValue(
              original.id,
              original.itemId,
              original.reviewText,
              original.reviewText == null ? 0 : original.reviewText.length()
      );
    }

  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.snippets;

import static co.decodable.sdk.pipeline.snippets.KeyedAppendStreamPurchaseOrderProcessingJob.PURCHASE_ORDERS_PROCESSED_STREAM;
import static co.decodable.sdk.pipeline.snippets.KeyedAppendStreamPurchaseOrderProcessingJob.PURCHASE_ORDERS_STREAM;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;
import co.decodable.sdk.pipeline.model.PurchaseOrder;
import co.decodable.sdk.pipeline.model.append.KeylessPurchaseOrder;
import co.decodable.sdk.pipeline.serde.DecodableRecordDeserializationSchema;
import co.decodable.sdk.pipeline.serde.DecodableRecordSerializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// spotless:off
@SourceStreams(PURCHASE_ORDERS_STREAM) // @start region="custom-pipeline"
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class KeylessAppendStreamPurchaseOrderProcessingJob {

  static final String PURCHASE_ORDERS_STREAM = "purchase-orders";
  static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // @highlight region regex=".*"
    DecodableStreamSource<KeylessPurchaseOrder> source = DecodableStreamSource.<KeylessPurchaseOrder>builder()
        .withStreamName(PURCHASE_ORDERS_STREAM)
        .withRecordDeserializationSchema(new DecodableRecordDeserializationSchema<>(KeylessPurchaseOrder.class))
        .build();

    DecodableStreamSink<KeylessPurchaseOrder> sink = DecodableStreamSink.<KeylessPurchaseOrder>builder()
        .withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
        .withRecordSerializationSchema(new DecodableRecordSerializationSchema<>(PurchaseOrder.class))
        .build();
    // @end

    DataStream<KeylessPurchaseOrder> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(),
                    PURCHASE_ORDERS_STREAM)
        .map(new PurchaseOrderProcessor());

    stream.sinkTo(sink).name(PURCHASE_ORDERS_PROCESSED_STREAM);

    env.execute("purchase order processor with keyless append streams");
  } // @end region="custom-pipeline"

  public static class PurchaseOrderProcessor extends RichMapFunction<KeylessPurchaseOrder, KeylessPurchaseOrder> {

    private static final long serialVersionUID = 1L;
    private Counter recordsProcessed;

    // @start region="metric-group"
    @Override
    public void open(Configuration parameters) throws Exception {
      recordsProcessed = getRuntimeContext()
              .getMetricGroup()
              .addGroup("DecodableMetrics")
              .counter("recordsProcessed", new SimpleCounter());
    }
    // @end region="metric-group"

    @Override
    public KeylessPurchaseOrder map(KeylessPurchaseOrder keylessPurchaseOrder) {
      var originalOrder = keylessPurchaseOrder.getValue();
      var resultingOrder = new PurchaseOrder(
              originalOrder.orderId,
              originalOrder.orderDate,
              originalOrder.customerName.toUpperCase(),
              originalOrder.price,
              originalOrder.productId,
              originalOrder.orderStatus
      );
      recordsProcessed.inc();
      return new KeylessPurchaseOrder(resultingOrder);
    }
  }
}
//spotless:on

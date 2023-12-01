/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.snippets;

import static co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob.PURCHASE_ORDERS_PROCESSED_STREAM;
import static co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob.PURCHASE_ORDERS_STREAM;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.PurchaseOrder;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// spotless:off
@SourceStreams(PURCHASE_ORDERS_STREAM) // @start region="custom-pipeline"
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class PurchaseOrderProcessingJob {

  static final String PURCHASE_ORDERS_STREAM = "purchase-orders";
  static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // @highlight region regex=".*"
    DecodableStreamSource<PurchaseOrder> source = DecodableStreamSource.<PurchaseOrder>builder()
        .withStreamName(PURCHASE_ORDERS_STREAM)
        .withDeserializationSchema(new JsonDeserializationSchema<>(PurchaseOrder.class))
        .build();

    DecodableStreamSink<PurchaseOrder> sink = DecodableStreamSink.<PurchaseOrder>builder()
        .withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
        .withSerializationSchema(new JsonSerializationSchema<>())
        .build();
    // @end

    DataStream<PurchaseOrder> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(),
                    "[stream-purchase-orders] Purchase Orders Source")
        .map(new PurchaseOrderProcessor());

    stream.sinkTo(sink).name("[stream-purchase-orders-processed] Purchase Orders Sink");

    env.execute("Purchase Order Processor");
  } // @end region="custom-pipeline"
  //spotless:on

  public static class PurchaseOrderProcessor extends RichMapFunction<PurchaseOrder, PurchaseOrder> {

    private static final long serialVersionUID = 1L;

    @Override
    public PurchaseOrder map(PurchaseOrder purchaseOrder) throws Exception {
      purchaseOrder.customerName = purchaseOrder.customerName.toUpperCase();
      return purchaseOrder;
    }
  }
}

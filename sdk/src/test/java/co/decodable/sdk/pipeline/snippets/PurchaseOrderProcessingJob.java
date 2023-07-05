/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.snippets;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.PurchaseOrder;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class PurchaseOrderProcessingJob {

  // spotless:off
  public static void main(String[] args) throws Exception { // @start region="custom-pipeline"
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // @highlight region regex=".*"
    DecodableStreamSource<PurchaseOrder> source = DecodableStreamSource.<PurchaseOrder>builder()
        .withStreamName("purchase-orders")
        .withDeserializationSchema(new JsonDeserializationSchema<>(PurchaseOrder.class))
        .build();

    DecodableStreamSink<PurchaseOrder> sink = DecodableStreamSink.<PurchaseOrder>builder()
        .withStreamName("purchase-orders-processed")
        .withSerializationSchema(new JsonSerializationSchema<>())
        .build();
    // @end

    DataStream<PurchaseOrder> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Purchase Orders Source")
        .map(new PurchaseOrderProcessor());

    stream.sinkTo(sink);

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

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
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class DataStreamJob {

  // spotless:off
  public static void main(String[] args) throws Exception { // @start region="custom-pipeline"
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // @highlight region regex=".*"
    DecodableStreamSource<String> source = DecodableStreamSource.builder()
        .withStreamName("purchase-orders")
        .build();

    DecodableStreamSink<String> sink = DecodableStreamSink.builder()
        .withStreamName("purchase-orders-processed")
        .build();
    // @end

    DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Purchase Orders Source")
        .map(new PurchaseOrderProcessor());

    stream.sinkTo(sink);

    env.execute("Purchase Order Processor");
  } // @end region="custom-pipeline"
  //spotless:on

  public static class PurchaseOrderProcessor extends RichMapFunction<String, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public String map(String value) throws Exception {
      return value;
    }
  }
}

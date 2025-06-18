/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import co.decodable.examples.cpdemo.model.append.KeyedPurchaseOrder;
import co.decodable.examples.cpdemo.model.OrderKey;
import co.decodable.examples.cpdemo.model.PurchaseOrder;
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

import static co.decodable.examples.cpdemo.KeylessDataStreamJob.PURCHASE_ORDERS_PROCESSED_STREAM;
import static co.decodable.examples.cpdemo.KeylessDataStreamJob.PURCHASE_ORDERS_STREAM;

@SourceStreams(PURCHASE_ORDERS_STREAM)
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class KeyedDataStreamJob {

  static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";
  static final String PURCHASE_ORDERS_STREAM = "purchase-orders";

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DecodableStreamSource<KeyedPurchaseOrder> source =
            DecodableStreamSource.<KeyedPurchaseOrder>builder()
                    .withStreamName(PURCHASE_ORDERS_STREAM)
                    .withRecordDeserializationSchema(new DecodableRecordDeserializationSchema<>(KeyedPurchaseOrder.class))
                    .build();

    DecodableStreamSink<KeyedPurchaseOrder> sink =
            DecodableStreamSink.<KeyedPurchaseOrder>builder()
                    .withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
                    .withRecordSerializationSchema(new DecodableRecordSerializationSchema<>(OrderKey.class, PurchaseOrder.class))
                    .build();

    DataStream<KeyedPurchaseOrder> stream =
            env.fromSource(source, WatermarkStrategy.noWatermarks(),
                            PURCHASE_ORDERS_STREAM)
                    .map(new NameConverter());

    stream.sinkTo(sink)
            .name(PURCHASE_ORDERS_PROCESSED_STREAM);

    env.execute("purchase order processor with keyed append streams");
  }

  public static class NameConverter extends RichMapFunction<KeyedPurchaseOrder, KeyedPurchaseOrder> {

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
    public KeyedPurchaseOrder map(KeyedPurchaseOrder order) throws Exception {
      var newOrderValue = new PurchaseOrder(
              order.getValue().orderId,
              order.getValue().orderDate,
              order.getValue().customerName.toUpperCase(),
              order.getValue().price,
              order.getValue().productId,
              order.getValue().orderStatus
      );
      recordsProcessed.inc();
      return new KeyedPurchaseOrder(order.getKey(),newOrderValue);
    }
  }
}

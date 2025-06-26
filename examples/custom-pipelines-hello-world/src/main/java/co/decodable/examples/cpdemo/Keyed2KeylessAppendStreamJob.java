/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import co.decodable.examples.cpdemo.model.OrderKey;
import co.decodable.examples.cpdemo.model.PurchaseOrder;
import co.decodable.examples.cpdemo.model.append.KeyedPurchaseOrder;
import co.decodable.examples.cpdemo.model.append.KeylessPurchaseOrder;
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

import static co.decodable.examples.cpdemo.KeylessAppendStreamJob.PURCHASE_ORDERS_PROCESSED_STREAM;
import static co.decodable.examples.cpdemo.KeylessAppendStreamJob.PURCHASE_ORDERS_STREAM;

@SourceStreams(PURCHASE_ORDERS_STREAM)
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class Keyed2KeylessAppendStreamJob {

  static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";
  static final String PURCHASE_ORDERS_STREAM = "purchase-orders";

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DecodableStreamSource<KeyedPurchaseOrder> source =
            DecodableStreamSource.<KeyedPurchaseOrder>builder()
                    .withStreamName(PURCHASE_ORDERS_STREAM)
                    .withRecordDeserializationSchema(new DecodableRecordDeserializationSchema<>(KeyedPurchaseOrder.class))
                    .build();

    DecodableStreamSink<KeylessPurchaseOrder> sink =
            DecodableStreamSink.<KeylessPurchaseOrder>builder()
                    .withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
                    .withRecordSerializationSchema(new DecodableRecordSerializationSchema<>(PurchaseOrder.class))
                    .build();

    DataStream<KeylessPurchaseOrder> stream =
            env.fromSource(source, WatermarkStrategy.noWatermarks(),
                            PURCHASE_ORDERS_STREAM)
                    .map(new NameConverter());

    stream.sinkTo(sink)
            .name(PURCHASE_ORDERS_PROCESSED_STREAM);

    env.execute("purchase order processor reading keyed/writing keyless append stream");
  }

  public static class NameConverter extends RichMapFunction<KeyedPurchaseOrder, KeylessPurchaseOrder> {

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
    public KeylessPurchaseOrder map(KeyedPurchaseOrder order) throws Exception {
      var orderValue = order.getValue();
      var newOrder = new PurchaseOrder(
							orderValue.orderId,
              orderValue.orderDate,
              orderValue.customerName.toUpperCase(),
              orderValue.price,
              orderValue.productId,
              orderValue.orderStatus
      );
      recordsProcessed.inc();
      return new KeylessPurchaseOrder(newOrder);
    }
  }
}

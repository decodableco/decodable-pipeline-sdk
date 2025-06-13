/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import co.decodable.examples.cpdemo.model.KeyedPurchaseOrder;
import co.decodable.examples.cpdemo.model.OrderKey;
import co.decodable.examples.cpdemo.model.PurchaseOrder;
import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;
import co.decodable.sdk.pipeline.serde.DecodableRecordDeserializationSchema;
import co.decodable.sdk.pipeline.serde.DecodableRecordSerializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;

@SourceStreams(KeyedTableAPIJob.PURCHASE_ORDERS_STREAM)
@SinkStreams(KeyedTableAPIJob.PURCHASE_ORDERS_PROCESSED_STREAM)
public class KeyedTableAPIJob {
  static final String PURCHASE_ORDERS_STREAM = "purchase-orders";
  static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";

  public static void main(String[] strings) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

    DecodableStreamSource<KeyedPurchaseOrder> source =
        DecodableStreamSource.<KeyedPurchaseOrder>builder()
            .withStreamName(PURCHASE_ORDERS_STREAM)
            .withRecordDeserializationSchema(new DecodableRecordDeserializationSchema<>(KeyedPurchaseOrder.class))
            .build();

    DecodableStreamSink<KeyedPurchaseOrder> sink =
        DecodableStreamSink.<KeyedPurchaseOrder>builder()
            .withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
                .withRecordSerializationSchema(new DecodableRecordSerializationSchema<>())
            .build();

    DataStream<KeyedPurchaseOrder> stream =
        env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "[stream-purchase-orders] Purchase Orders Source");

    Table inputTable = tableEnv.fromDataStream(
        stream,
        Schema.newBuilder()
                .column("key", DataTypes.STRUCTURED(
                        OrderKey.class,
                        DataTypes.FIELD("orderId",DataTypes.BIGINT()))
                )
                .column("value", DataTypes.STRUCTURED(
                        PurchaseOrder.class,
                        DataTypes.FIELD("orderId",DataTypes.BIGINT()),
                        DataTypes.FIELD("orderDate", DataTypes.STRING()),
                        DataTypes.FIELD("customerName", DataTypes.STRING()),
                        DataTypes.FIELD("price", DataTypes.DOUBLE()),
                        DataTypes.FIELD("productId", DataTypes.BIGINT()),
                        DataTypes.FIELD("orderStatus", DataTypes.BOOLEAN()))
                )
        .build()
    );
    
    tableEnv.createTemporaryView("purchase_orders", inputTable);

    // register a UDF
    tableEnv.createTemporarySystemFunction("upper_case", UpperCase.class);

    Table resultTable =
        tableEnv.sqlQuery(
          "SELECT `key`, ROW(orderId, orderDate, customerNameUC, price, productId, orderStatus) as `value` FROM (SELECT `key`, `value`.*, upper_case(`value`.customerName) as customerNameUC FROM purchase_orders)");

    DataStream<KeyedPurchaseOrder> resultStream =
        tableEnv.toDataStream(
            resultTable,
            DataTypes.STRUCTURED(
                KeyedPurchaseOrder.class,
                    DataTypes.FIELD("key", DataTypes.STRUCTURED(
                            OrderKey.class,
                            DataTypes.FIELD("orderId",DataTypes.BIGINT()))
                    ),
                    DataTypes.FIELD("value", DataTypes.STRUCTURED(
                            PurchaseOrder.class,
                            DataTypes.FIELD("orderId",DataTypes.BIGINT()),
                            DataTypes.FIELD("orderDate", DataTypes.STRING()),
                            DataTypes.FIELD("customerName", DataTypes.STRING()),
                            DataTypes.FIELD("price", DataTypes.DOUBLE()),
                            DataTypes.FIELD("productId", DataTypes.BIGINT()),
                            DataTypes.FIELD("orderStatus", DataTypes.BOOLEAN()))
                    )
            )
        );

    resultStream.sinkTo(sink).name("[stream-purchase-orders-processed] Purchase Orders Sink");

    env.execute("Purchase Order Processor");
  }

  // UDF
  @FunctionHint(output = @DataTypeHint("STRING"))
  public static class UpperCase extends ScalarFunction {
    public UpperCase() {}

    @SuppressWarnings("unused")
    public String eval(String input) {
      if (input == null) {
        return null;
      }
      return input.toUpperCase();
    }
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;

@SourceStreams(TableAPIJob.PURCHASE_ORDERS_STREAM)
@SinkStreams(TableAPIJob.PURCHASE_ORDERS_PROCESSED_STREAM)
public class TableAPIJob {
  static final String PURCHASE_ORDERS_STREAM = "purchase-orders";
  static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";

  public static void main(String[] strings) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

    DecodableStreamSource<PurchaseOrder> source =
        DecodableStreamSource.<PurchaseOrder>builder()
            .withStreamName(PURCHASE_ORDERS_STREAM)
            .withDeserializationSchema(new JsonDeserializationSchema<>(PurchaseOrder.class))
            .build();
    DecodableStreamSink<PurchaseOrder> sink =
        DecodableStreamSink.<PurchaseOrder>builder()
            .withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
            .withSerializationSchema(new JsonSerializationSchema<>())
            .build();

    DataStream<PurchaseOrder> stream =
        env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "[stream-purchase-orders] Purchase Orders Source");

    Table inputTable = tableEnv.fromDataStream(
        stream,
        Schema.newBuilder()
          .column("orderId", DataTypes.BIGINT())
          .column("orderDate", DataTypes.STRING())
          .column("customerName", DataTypes.STRING())
          .column("price", DataTypes.DOUBLE())
          .column("productId", DataTypes.BIGINT())
          .column("orderStatus", DataTypes.BOOLEAN())
        .build()
    );
    
    tableEnv.createTemporaryView("purchase_orders", inputTable);
    
    // register a UDF
    tableEnv.createTemporarySystemFunction("upper_case", UpperCase.class);

    Table resultTable =
        tableEnv.sqlQuery(
            "SELECT orderId, orderDate, upper_case(customerName) AS customerName, price, productId, orderStatus FROM purchase_orders");

    DataStream<PurchaseOrder> resultStream =
        tableEnv.toDataStream(
            resultTable,
            DataTypes.STRUCTURED(
                PurchaseOrder.class,
                DataTypes.FIELD("orderId", DataTypes.BIGINT()),
                DataTypes.FIELD("orderDate", DataTypes.STRING()),
                DataTypes.FIELD("customerName", DataTypes.STRING()),
                DataTypes.FIELD("price", DataTypes.DOUBLE()),
                DataTypes.FIELD("productId", DataTypes.BIGINT()),
                DataTypes.FIELD("orderStatus", DataTypes.BOOLEAN())));

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

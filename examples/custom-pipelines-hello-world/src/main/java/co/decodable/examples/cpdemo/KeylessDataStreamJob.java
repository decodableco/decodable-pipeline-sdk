/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import static co.decodable.examples.cpdemo.KeylessDataStreamJob.PURCHASE_ORDERS_PROCESSED_STREAM;
import static co.decodable.examples.cpdemo.KeylessDataStreamJob.PURCHASE_ORDERS_STREAM;

import co.decodable.examples.cpdemo.model.KeylessPurchaseOrder;
import co.decodable.examples.cpdemo.model.PurchaseOrder;
import co.decodable.sdk.pipeline.serde.DecodableRecordDeserializationSchema;
import co.decodable.sdk.pipeline.serde.DecodableRecordSerializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams(PURCHASE_ORDERS_STREAM)
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class KeylessDataStreamJob {

	static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";
	static final String PURCHASE_ORDERS_STREAM = "purchase-orders";

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DecodableStreamSource<KeylessPurchaseOrder> source =
				DecodableStreamSource.<KeylessPurchaseOrder>builder()
					.withStreamName(PURCHASE_ORDERS_STREAM)
					.withRecordDeserializationSchema(new DecodableRecordDeserializationSchema<>(KeylessPurchaseOrder.class))
					.build();

		DecodableStreamSink<KeylessPurchaseOrder> sink =
			DecodableStreamSink.<KeylessPurchaseOrder>builder()
				.withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
				.withRecordSerializationSchema(new DecodableRecordSerializationSchema<>())
				.build();

		DataStream<KeylessPurchaseOrder> stream =
			env.fromSource(source, WatermarkStrategy.noWatermarks(),
 				"[stream-purchase-orders] Purchase Orders Source")
				.map(new NameConverter());

		stream.sinkTo(sink)
			.name("[stream-purchase-orders-processed] Purchase Orders Sink");

		env.execute("Purchase Order Processor");
	}

	public static class NameConverter extends RichMapFunction<KeylessPurchaseOrder, KeylessPurchaseOrder> {

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
		public KeylessPurchaseOrder map(KeylessPurchaseOrder order) throws Exception {
			var newOrder = new PurchaseOrder(
					order.getValue().orderId,
					order.getValue().orderDate,
					order.getValue().customerName.toUpperCase(),
					order.getValue().price,
					order.getValue().productId,
					order.getValue().orderStatus
			);
			recordsProcessed.inc();
			return new KeylessPurchaseOrder(newOrder);
		}
	}
}

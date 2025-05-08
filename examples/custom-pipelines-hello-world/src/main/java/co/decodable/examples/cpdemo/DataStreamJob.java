/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import static co.decodable.examples.cpdemo.DataStreamJob.PURCHASE_ORDERS_PROCESSED_STREAM;
import static co.decodable.examples.cpdemo.DataStreamJob.PURCHASE_ORDERS_STREAM;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.util.jackson.JacksonMapperFactory;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams(PURCHASE_ORDERS_STREAM)
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class DataStreamJob {

	static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";
	static final String PURCHASE_ORDERS_STREAM = "purchase-orders";

	static final ObjectMapper OBJECT_MAPPER = JacksonMapperFactory.createObjectMapper();

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DecodableStreamSource<PurchaseOrder> source =
				DecodableStreamSource.<PurchaseOrder>builder()
					.withStreamName(PURCHASE_ORDERS_STREAM)
					.withDeserializationSchema(new JsonDeserializationSchema<>(PurchaseOrder.class, () -> OBJECT_MAPPER))
					.build();

		DecodableStreamSink<PurchaseOrder> sink =
			DecodableStreamSink.<PurchaseOrder>builder()
				.withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
				.withSerializationSchema(new JsonSerializationSchema<>(() -> OBJECT_MAPPER))
				.build();

		DataStream<PurchaseOrder> stream =
			env.fromSource(source, WatermarkStrategy.noWatermarks(),
 				"[stream-purchase-orders] Purchase Orders Source")
				.map(new NameConverter());

		stream.sinkTo(sink)
			.name("[stream-purchase-orders-processed] Purchase Orders Sink");

		env.execute("Purchase Order Processor");
	}

	public static class NameConverter extends RichMapFunction<PurchaseOrder, PurchaseOrder> {

		private static final long serialVersionUID = 1L;

		private transient ObjectMapper mapper;
		private Counter recordsProcessed;

		@Override
		public void open(Configuration parameters) throws Exception {
			mapper = new ObjectMapper();
			recordsProcessed = getRuntimeContext()
				.getMetricGroup()
				.addGroup("DecodableMetrics")
				.counter("recordsProcessed", new SimpleCounter());
		}

		@Override
		public PurchaseOrder map(PurchaseOrder order) throws Exception {
			var newOrder = new PurchaseOrder(
					order.orderId,
					order.orderDate,
					order.customerName.toUpperCase(),
					order.price,
					order.productId,
					order.orderStatus
			);
			recordsProcessed.inc();
			return newOrder;
		}
	}
}

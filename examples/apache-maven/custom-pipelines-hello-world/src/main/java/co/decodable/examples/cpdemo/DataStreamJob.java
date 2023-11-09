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
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.decodable.sdk.pipeline.DecodableDataStreamSinkBuilder;
import co.decodable.sdk.pipeline.DecodableDataStreamSourceBuilder;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams(PURCHASE_ORDERS_STREAM)
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class DataStreamJob {

	static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";
	static final String PURCHASE_ORDERS_STREAM = "purchase-orders";

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<String> stream = new DecodableDataStreamSourceBuilder<String>(
				env, WatermarkStrategy.noWatermarks()
		)
			.withStreamName(PURCHASE_ORDERS_STREAM)
			.withDeserializationSchema(new SimpleStringSchema())
			.build()
				.map(new NameConverter());

		new DecodableDataStreamSinkBuilder<String>(
				stream
		)
				.withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
				.withSerializationSchema(new SimpleStringSchema())
				.build();

		env.execute("Purchase Order Processor");
	}

	public static class NameConverter extends RichMapFunction<String, String> {

		private static final long serialVersionUID = 1L;

		private transient ObjectMapper mapper;

		@Override
		public void open(Configuration parameters) throws Exception {
			mapper = new ObjectMapper();
		}

		@Override
		public String map(String value) throws Exception {
			ObjectNode purchaseOrder = (ObjectNode) mapper.readTree(value);
			purchaseOrder.put("customer_name", purchaseOrder.get("customer_name").asText().toUpperCase());
			return mapper.writeValueAsString(purchaseOrder);
		}
	}
}

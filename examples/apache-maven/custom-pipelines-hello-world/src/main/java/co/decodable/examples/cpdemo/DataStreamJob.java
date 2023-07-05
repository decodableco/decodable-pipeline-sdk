/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;

public class DataStreamJob {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DecodableStreamSource<String> source =
				DecodableStreamSource.<String>builder()
					.withStreamName("purchase-orders")
					.withDeserializationSchema(new SimpleStringSchema())
					.build();

		DecodableStreamSink<String> sink =
			DecodableStreamSink.<String>builder()
				.withStreamName("purchase-orders-processed")
				.withSerializationSchema(new SimpleStringSchema())
				.build();

		DataStream<String> stream =
			env.fromSource(source, WatermarkStrategy.noWatermarks(), "Purchase Orders Source")
				.map(new NameConverter());

		stream.sinkTo(sink);

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

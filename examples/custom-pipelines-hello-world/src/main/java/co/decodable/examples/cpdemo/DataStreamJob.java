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
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.SimpleCounter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams(PURCHASE_ORDERS_STREAM)
@SinkStreams(PURCHASE_ORDERS_PROCESSED_STREAM)
public class DataStreamJob {

	static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";
	static final String PURCHASE_ORDERS_STREAM = "purchase-orders";

	private final  Source<String,?,?> source;
    private final Sink<String> sink;

	public DataStreamJob(Source<String,?,?> source, Sink<String> sink) {
		this.source = source;
		this.sink = sink;
	}

	public void run() throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<String> stream =
			env.fromSource(source, WatermarkStrategy.noWatermarks(),
 				"[stream-purchase-orders] Purchase Orders Source")
				.map(new NameConverter());

		stream.sinkTo(sink)
			.name("[stream-purchase-orders-processed] Purchase Orders Sink");

		env.execute("Purchase Order Processor");
	}
	
	public static void main(String[] args) throws Exception {
		DecodableStreamSource<String> source =
				DecodableStreamSource.<String>builder()
					.withStreamName(PURCHASE_ORDERS_STREAM)
					.withDeserializationSchema(new SimpleStringSchema())
					.build();

		DecodableStreamSink<String> sink =
			DecodableStreamSink.<String>builder()
				.withStreamName(PURCHASE_ORDERS_PROCESSED_STREAM)
				.withSerializationSchema(new SimpleStringSchema())
				.build();

		var job = new DataStreamJob(source, sink);
		job.run();
	}

	public static class NameConverter extends RichMapFunction<String, String> {

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
		public String map(String value) throws Exception {
			ObjectNode purchaseOrder = (ObjectNode) mapper.readTree(value);
			purchaseOrder.put("customer_name", purchaseOrder.get("customer_name").asText().toUpperCase());
			recordsProcessed.inc();
			return mapper.writeValueAsString(purchaseOrder);
		}
	}
}

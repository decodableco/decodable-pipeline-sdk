/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

@Incubating
public class DecodableDataStreamSourceBuilder<T> {
  private DecodableStreamSourceBuilder<T> decodableStreamSourceBuilder;
  private StreamExecutionEnvironment env;
  private WatermarkStrategy watermarkStrategy;
  private String streamId;
  private String streamName;

  public DecodableDataStreamSourceBuilder(
      StreamExecutionEnvironment env, WatermarkStrategy watermarkStrategy) {
    this.env = env;
    this.watermarkStrategy = watermarkStrategy;
    this.decodableStreamSourceBuilder = DecodableStreamSource.<T>builder();
  }

  public DecodableDataStreamSourceBuilder<T> withStreamName(String streamName) {
    this.streamName = streamName;
    decodableStreamSourceBuilder.withStreamName(streamName);
    return this;
  }

  public DecodableDataStreamSourceBuilder<T> withStreamId(String streamId) {
    this.streamId = streamId;
    decodableStreamSourceBuilder.withStreamId(streamId);
    return this;
  }

  public DecodableDataStreamSourceBuilder<T> withStartupMode(StartupMode startupMode) {
    decodableStreamSourceBuilder.withStartupMode(startupMode);
    return this;
  }

  public DecodableDataStreamSourceBuilder<T> withDeserializationSchema(
      DeserializationSchema<T> deserializationSchema) {
    decodableStreamSourceBuilder.withDeserializationSchema(deserializationSchema);
    return this;
  }

  public DataStreamSource<T> build() {
    var name =
        streamId == null
            ? String.format("[decodable-pipeline-sdk-stream-name] %s", streamName)
            : String.format("[decodable-pipeline-sdk-stream-id] %s", streamId);
    return env.fromSource(decodableStreamSourceBuilder.build(), watermarkStrategy, name);
  }
}

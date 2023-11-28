/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableStreamSource;
import co.decodable.sdk.pipeline.DecodableStreamSourceBuilder;
import co.decodable.sdk.pipeline.EnvironmentAccess;
import co.decodable.sdk.pipeline.StartupMode;
import co.decodable.sdk.pipeline.internal.config.StreamConfig;
import co.decodable.sdk.pipeline.internal.config.StreamConfigMapping;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class DecodableStreamSourceBuilderImpl<T> implements DecodableStreamSourceBuilder<T> {

  private StreamExecutionEnvironment streamExecutionEnvironment;
  private WatermarkStrategy<T> watermarkStrategy;
  private String streamName;
  private StartupMode startupMode;
  private DeserializationSchema<T> deserializationSchema;
  private String name;

  @Override
  public DecodableStreamSourceBuilder<T> withStreamExecutionEnvironment(
      StreamExecutionEnvironment streamExecutionEnvironment) {
    this.streamExecutionEnvironment = streamExecutionEnvironment;
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder<T> withWatermarkStrategy(
      WatermarkStrategy<T> watermarkStrategy) {
    this.watermarkStrategy = watermarkStrategy;
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder<T> withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder<T> withStreamName(String streamName) {
    this.streamName = streamName;
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder<T> withStartupMode(StartupMode startupMode) {
    this.startupMode = startupMode;
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder<T> withDeserializationSchema(
      DeserializationSchema<T> deserializationSchema) {
    this.deserializationSchema = deserializationSchema;
    return this;
  }

  @Override
  public DataStream<T> build() {
    Objects.requireNonNull(deserializationSchema, "deserializationSchema");

    Map<String, String> environment =
        EnvironmentAccess.getEnvironment().getEnvironmentConfiguration();

    StreamConfig streamConfig = new StreamConfigMapping(environment).determineConfig(streamName);

    KafkaSourceBuilder<T> builder =
        KafkaSource.<T>builder()
            .setBootstrapServers(streamConfig.bootstrapServers())
            .setTopics(streamConfig.topic())
            .setProperties(toProperties(streamConfig.kafkaProperties()))
            .setValueOnlyDeserializer(deserializationSchema);

    if (streamConfig.startupMode() != null) {
      builder.setStartingOffsets(toOffsetsInitializer(streamConfig.startupMode()));
    } else if (startupMode != null) {
      builder.setStartingOffsets(toOffsetsInitializer(startupMode));
    }

    KafkaSource<T> delegate = builder.build();

    DecodableStreamSource<T> decodableStreamSource = new DecodableStreamSourceImpl<T>(delegate);

    if (streamExecutionEnvironment == null) {
      throw new IllegalArgumentException("Argument streamExecutionEnvironment is required.");
    }
    if (watermarkStrategy == null) {
      throw new IllegalArgumentException("Argument watermarkStrategy is required.");
    }

    String operatorName = String.format("[decodable-pipeline-sdk-stream-%s] %s", streamName, name);
    return streamExecutionEnvironment.fromSource(
        decodableStreamSource, watermarkStrategy, operatorName);
  }

  private static Properties toProperties(Map<String, String> map) {
    Properties p = new Properties();
    p.putAll(map);
    return p;
  }

  private OffsetsInitializer toOffsetsInitializer(StartupMode startupMode) {
    switch (startupMode) {
      case EARLIEST_OFFSET:
        return OffsetsInitializer.earliest();
      case LATEST_OFFSET:
        return OffsetsInitializer.latest();
      default:
        throw new IllegalArgumentException("Unexpected startup mode: " + startupMode);
    }
  }
}

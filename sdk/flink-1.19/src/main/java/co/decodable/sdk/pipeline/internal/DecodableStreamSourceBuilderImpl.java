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
import co.decodable.sdk.pipeline.serde.DecodableRecordDeserializationSchema;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;

public class DecodableStreamSourceBuilderImpl<T> implements DecodableStreamSourceBuilder<T> {

  private String streamId;
  private String streamName;
  private StartupMode startupMode;
  private KafkaRecordDeserializationSchema<?> recordDeserializationSchema;

  @Override
  public DecodableStreamSourceBuilder<T> withStreamName(String streamName) {
    this.streamName = streamName;
    return this;
  }

  @Override
  @Deprecated
  public DecodableStreamSourceBuilder<T> withStreamId(String streamId) {
    this.streamId = streamId;
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
    this.recordDeserializationSchema =
        KafkaRecordDeserializationSchema.valueOnly(deserializationSchema);
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder<T> withRecordDeserializationSchema(
      DecodableRecordDeserializationSchema<?> recordDeserializationSchema) {
    this.recordDeserializationSchema = recordDeserializationSchema;
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DecodableStreamSource<T> build() {
    Objects.requireNonNull(recordDeserializationSchema, "deserialization schema must be specified");

    Map<String, String> environment =
        EnvironmentAccess.getEnvironment().getEnvironmentConfiguration();

    StreamConfig streamConfig =
        new StreamConfigMapping(environment).determineConfig(streamName, streamId);

    KafkaSourceBuilder<T> builder =
        KafkaSource.<T>builder()
            .setBootstrapServers(streamConfig.bootstrapServers())
            .setTopics(streamConfig.topic())
            .setProperties(toProperties(streamConfig.kafkaProperties()))
            .setDeserializer((KafkaRecordDeserializationSchema<T>) recordDeserializationSchema);

    if (streamConfig.startupMode() != null) {
      builder.setStartingOffsets(toOffsetsInitializer(streamConfig.startupMode()));
    } else if (startupMode != null) {
      builder.setStartingOffsets(toOffsetsInitializer(startupMode));
    }

    KafkaSource<T> delegate = builder.build();

    return new DecodableStreamSourceImpl<T>(delegate);
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

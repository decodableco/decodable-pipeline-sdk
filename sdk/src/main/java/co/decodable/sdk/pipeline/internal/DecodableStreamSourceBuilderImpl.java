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
import java.util.Properties;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

public class DecodableStreamSourceBuilderImpl implements DecodableStreamSourceBuilder {

  private String streamId;
  private String streamName;
  private StartupMode startupMode;

  @Override
  public DecodableStreamSourceBuilder withStreamName(String streamName) {
    this.streamName = streamName;
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder withStreamId(String streamId) {
    this.streamId = streamId;
    return this;
  }

  @Override
  public DecodableStreamSourceBuilder withStartupMode(StartupMode startupMode) {
    this.startupMode = startupMode;
    return this;
  }

  @Override
  public DecodableStreamSource<String> build() {
    Map<String, String> environment =
        EnvironmentAccess.getEnvironment().getEnvironmentConfiguration();

    StreamConfig streamConfig =
        new StreamConfigMapping(environment).determineConfig(streamName, streamId);

    KafkaSourceBuilder<String> builder =
        KafkaSource.<String>builder()
            .setBootstrapServers(streamConfig.bootstrapServers())
            .setTopics(streamConfig.topic())
            .setProperties(toProperties(streamConfig.kafkaProperties()))
            .setValueOnlyDeserializer(new SimpleStringSchema());

    if (streamConfig.startupMode() != null) {
      builder.setStartingOffsets(toOffsetsInitializer(streamConfig.startupMode()));
    } else if (startupMode != null) {
      builder.setStartingOffsets(toOffsetsInitializer(startupMode));
    }

    KafkaSource<String> delegate = builder.build();

    return new DecodableStreamSourceImpl<String>(delegate);
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

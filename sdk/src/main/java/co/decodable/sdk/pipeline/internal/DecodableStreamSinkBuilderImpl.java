/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableStreamSinkBuilder;
import co.decodable.sdk.pipeline.EnvironmentAccess;
import co.decodable.sdk.pipeline.internal.config.StreamConfig;
import co.decodable.sdk.pipeline.internal.config.StreamConfigMapping;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;

public class DecodableStreamSinkBuilderImpl<T> implements DecodableStreamSinkBuilder<T> {

  private String streamId;
  private String streamName;
  private SerializationSchema<T> serializationSchema;

  @Override
  public DecodableStreamSinkBuilder<T> withStreamName(String streamName) {
    this.streamName = streamName;
    return this;
  }

  @Override
  public DecodableStreamSinkBuilder<T> withStreamId(String streamId) {
    this.streamId = streamId;
    return this;
  }

  @Override
  public DecodableStreamSinkBuilder<T> withSerializationSchema(
      SerializationSchema<T> serializationSchema) {
    this.serializationSchema = serializationSchema;
    return this;
  }

  @Override
  public DecodableStreamSink<T> build() {
    Objects.requireNonNull(serializationSchema, "serializationSchema");

    Map<String, String> environment =
        EnvironmentAccess.getEnvironment().getEnvironmentConfiguration();

    StreamConfig streamConfig =
        new StreamConfigMapping(environment).determineConfig(streamName, streamId);

    KafkaSink<T> delegate =
        KafkaSink.<T>builder()
            .setBootstrapServers(streamConfig.bootstrapServers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder()
                    .setTopic(streamConfig.topic())
                    .setValueSerializationSchema(serializationSchema)
                    .build())
            .setDeliveryGuarantee(
                "exactly-once".equals(streamConfig.deliveryGuarantee())
                    ? DeliveryGuarantee.EXACTLY_ONCE
                    : "at-least-once".equals(streamConfig.deliveryGuarantee())
                        ? DeliveryGuarantee.AT_LEAST_ONCE
                        : DeliveryGuarantee.NONE)
            .setTransactionalIdPrefix(streamConfig.transactionalIdPrefix())
            .setKafkaProducerConfig(toProperties(streamConfig.kafkaProperties()))
            .build();

    return new DecodableStreamSinkImpl<T>(delegate);
  }

  private static Properties toProperties(Map<String, String> map) {
    Properties p = new Properties();
    p.putAll(map);
    return p;
  }
}

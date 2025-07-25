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
import co.decodable.sdk.pipeline.serde.DecodableRecordSerializationSchema;
import java.util.Map;
import java.util.Properties;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;

public class DecodableStreamSinkBuilderImpl<T> implements DecodableStreamSinkBuilder<T> {

  private String streamId;
  private String streamName;
  private SerializationSchema<T> serializationSchema;
  private DecodableRecordSerializationSchema<?> recordSerializationSchema;

  @Override
  public DecodableStreamSinkBuilder<T> withStreamName(String streamName) {
    this.streamName = streamName;
    return this;
  }

  @Override
  @Deprecated
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
  public DecodableStreamSinkBuilder<T> withRecordSerializationSchema(
      DecodableRecordSerializationSchema<?> recordSerializationSchema) {
    this.recordSerializationSchema = recordSerializationSchema;
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DecodableStreamSink<T> build() {
    if (serializationSchema != null && recordSerializationSchema != null) {
      throw new IllegalStateException(
          "only one of value serialization or record serialization schema may be specified");
    }

    if (serializationSchema == null && recordSerializationSchema == null) {
      throw new IllegalStateException(
          "either value serialization or record serialization schema must be specified");
    }

    Map<String, String> environment =
        EnvironmentAccess.getEnvironment().getEnvironmentConfiguration();

    StreamConfig streamConfig =
        new StreamConfigMapping(environment).determineConfig(streamName, streamId);

    KafkaSinkBuilder<T> builder =
        KafkaSink.<T>builder()
            .setBootstrapServers(streamConfig.bootstrapServers())
            .setDeliveryGuarantee(
                "exactly-once".equals(streamConfig.deliveryGuarantee())
                    ? DeliveryGuarantee.EXACTLY_ONCE
                    : "at-least-once".equals(streamConfig.deliveryGuarantee())
                        ? DeliveryGuarantee.AT_LEAST_ONCE
                        : DeliveryGuarantee.NONE)
            .setTransactionalIdPrefix(streamConfig.transactionalIdPrefix())
            .setKafkaProducerConfig(toProperties(streamConfig.kafkaProperties()));

    if (serializationSchema != null) {
      builder.setRecordSerializer(
          KafkaRecordSerializationSchema.builder()
              .setTopic(streamConfig.topic())
              .setValueSerializationSchema(serializationSchema)
              .build());
    }

    if (recordSerializationSchema != null) {
      if (recordSerializationSchema.getTargetTopic() == null) {
        recordSerializationSchema.setTargetTopic(streamConfig.topic());
      }
      builder.setRecordSerializer((KafkaRecordSerializationSchema<T>) recordSerializationSchema);
    }

    KafkaSink<T> delegate = builder.build();
    return new DecodableStreamSinkImpl<T>(delegate);
  }

  private static Properties toProperties(Map<String, String> map) {
    Properties p = new Properties();
    p.putAll(map);
    return p;
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

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
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;

public class DecodableStreamSinkBuilderImpl<T> implements DecodableStreamSinkBuilder<T> {

  private DataStream<T> dataStream;
  private String streamName;
  private SerializationSchema<T> serializationSchema;
  private String name;

  @Override
  public DecodableStreamSinkBuilder<T> withDataStream(DataStream<T> dataStream) {
    this.dataStream = dataStream;
    return this;
  }

  @Override
  public DecodableStreamSinkBuilder<T> withStreamName(String streamName) {
    this.streamName = streamName;
    return this;
  }

  @Override
  public DecodableStreamSinkBuilder<T> withSerializationSchema(
      SerializationSchema<T> serializationSchema) {
    this.serializationSchema = serializationSchema;
    return this;
  }

  @Override
  public DecodableStreamSinkBuilder<T> withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public DataStreamSink<T> build() {
    Objects.requireNonNull(serializationSchema, "serializationSchema");

    Map<String, String> environment =
        EnvironmentAccess.getEnvironment().getEnvironmentConfiguration();

    StreamConfig streamConfig = new StreamConfigMapping(environment).determineConfig(streamName);

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

    var decodableStreamSink = new DecodableStreamSinkImpl<T>(delegate);

    if (dataStream == null) {
      throw new IllegalArgumentException("Argument dataStream is required.");
    }

    var operatorName = String.format("[decodable-pipeline-sdk-stream-%s] %s", streamName, name);
    return dataStream.sinkTo(decodableStreamSink).name(operatorName);
  }

  private static Properties toProperties(Map<String, String> map) {
    Properties p = new Properties();
    p.putAll(map);
    return p;
  }
}

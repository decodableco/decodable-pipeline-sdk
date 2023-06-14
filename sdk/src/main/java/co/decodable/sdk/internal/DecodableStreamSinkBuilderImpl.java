/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.internal;

import co.decodable.sdk.DecodableStreamSink;
import co.decodable.sdk.DecodableStreamSinkBuilder;
import co.decodable.sdk.Environment;
import co.decodable.sdk.internal.config.StreamConfig;
import co.decodable.sdk.internal.config.StreamConfigMapping;
import java.util.Map;
import java.util.Properties;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;

public class DecodableStreamSinkBuilderImpl implements DecodableStreamSinkBuilder {

  private String streamId;
  private String streamName;

  @Override
  public DecodableStreamSinkBuilder withStreamName(String streamName) {
    this.streamName = streamName;
    return this;
  }

  @Override
  public DecodableStreamSinkBuilder withStreamId(String streamId) {
    this.streamId = streamId;
    return this;
  }

  @Override
  public DecodableStreamSink<String> build() {
    Map<String, String> environment = Environment.getEnvironmentConfiguration();

    StreamConfig streamConfig =
        new StreamConfigMapping(environment).determineConfig(streamName, streamId);

    KafkaSink<String> delegate =
        KafkaSink.<String>builder()
            .setBootstrapServers(streamConfig.bootstrapServers())
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder()
                    .setTopic(streamConfig.topic())
                    .setValueSerializationSchema(new SimpleStringSchema())
                    .build())
            .setDeliverGuarantee(
                "exactly-once".equals(streamConfig.deliveryGuarantee())
                    ? DeliveryGuarantee.EXACTLY_ONCE
                    : "at-least-once".equals(streamConfig.deliveryGuarantee())
                        ? DeliveryGuarantee.AT_LEAST_ONCE
                        : DeliveryGuarantee.NONE)
            .setTransactionalIdPrefix(streamConfig.transactionalIdPrefix())
            .setKafkaProducerConfig(toProperties(streamConfig.kafkaProperties()))
            .build();

    return new DecodableStreamSinkImpl<String>(delegate);
  }

  private static Properties toProperties(Map<String, String> map) {
    Properties p = new Properties();
    p.putAll(map);
    return p;
  }
}

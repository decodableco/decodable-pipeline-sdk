/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.serde;

import co.decodable.sdk.pipeline.DecodableKeyedStreamRecord;
import java.util.Objects;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;

public final class DecodableRecordSerializationSchema<T extends DecodableKeyedStreamRecord<?, ?>>
    implements KafkaRecordSerializationSchema<T> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final Class<?> keyType;
  private final Class<?> valueType;
  private String targetTopic;

  public DecodableRecordSerializationSchema(Class<?> valueType) {
    this.keyType = null;
    this.valueType = Objects.requireNonNull(valueType, "valueType must not be null");
  }

  public DecodableRecordSerializationSchema(String targetTopic, Class<?> valueType) {
    if (targetTopic == null || targetTopic.isBlank()) {
      throw new IllegalArgumentException("targetTopic must not be null or blank");
    }
    this.targetTopic = targetTopic;
    this.keyType = null;
    this.valueType = Objects.requireNonNull(valueType, "valueType must not be null");
  }

  public DecodableRecordSerializationSchema(Class<?> keyType, Class<?> valueType) {
    this.keyType = Objects.requireNonNull(keyType, "keyType must not be null");
    this.valueType = Objects.requireNonNull(valueType, "valueType must not be null");
    SerializationConstraintsValidator.checkAllKeyFieldsPresentInValue(this.keyType, this.valueType);
  }

  public DecodableRecordSerializationSchema(
      String targetTopic, Class<?> keyType, Class<?> valueType) {
    if (targetTopic == null || targetTopic.isBlank()) {
      throw new IllegalArgumentException("targetTopic must not be null or blank");
    }
    this.targetTopic = targetTopic;
    this.keyType = Objects.requireNonNull(keyType, "keyType must not be null");
    this.valueType = Objects.requireNonNull(valueType, "valueType must not be null");
    SerializationConstraintsValidator.checkAllKeyFieldsPresentInValue(this.keyType, this.valueType);
  }

  public String getTargetTopic() {
    return targetTopic;
  }

  public void setTargetTopic(String targetTopic) {
    if (targetTopic == null || targetTopic.isBlank()) {
      throw new IllegalArgumentException("targetTopic must not be null or blank");
    }
    this.targetTopic = targetTopic;
  }

  @Override
  public void open(SerializationSchema.InitializationContext context, KafkaSinkContext sinkContext)
      throws Exception {
    KafkaRecordSerializationSchema.super.open(context, sinkContext);
  }

  @Override
  public ProducerRecord<byte[], byte[]> serialize(
      T element, KafkaSinkContext context, Long timestamp) {
    try {
      Objects.requireNonNull(targetTopic, "target topic must not be missing");
      return new ProducerRecord<>(
          targetTopic,
          (keyType != null && !Void.class.equals(keyType))
              ? OBJECT_MAPPER.writeValueAsBytes(element.getKey())
              : null,
          OBJECT_MAPPER.writeValueAsBytes(element.getValue()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("failed to serialize record: %s", element), e);
    }
  }
}

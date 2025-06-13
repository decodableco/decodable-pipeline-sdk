/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.serde;

import co.decodable.sdk.pipeline.DecodableAbstractStreamRecord;
import java.util.Objects;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;

public final class DecodableRecordSerializationSchema<T extends DecodableAbstractStreamRecord<?, ?>>
    implements KafkaRecordSerializationSchema<T> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private String targetTopic;

  public DecodableRecordSerializationSchema() {}

  public DecodableRecordSerializationSchema(String targetTopic) {
    this.targetTopic = targetTopic;
  }

  public String getTargetTopic() {
    return targetTopic;
  }

  public void setTargetTopic(String targetTopic) {
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
          OBJECT_MAPPER.writeValueAsBytes(element.getKey()),
          OBJECT_MAPPER.writeValueAsBytes(element.getValue()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(String.format("Could not serialize record '%s'.", element), e);
    }
  }
}

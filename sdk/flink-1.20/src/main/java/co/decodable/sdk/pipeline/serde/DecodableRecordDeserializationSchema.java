/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.serde;

import static co.decodable.sdk.pipeline.DecodableKeyedStreamRecord.DEFAULT_KEY_FIELD_NAME;
import static co.decodable.sdk.pipeline.DecodableKeyedStreamRecord.DEFAULT_VALUE_FIELD_NAME;

import co.decodable.sdk.pipeline.DecodableKeyedStreamRecord;
import java.io.IOException;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public final class DecodableRecordDeserializationSchema<T extends DecodableKeyedStreamRecord<?, ?>>
    implements KafkaRecordDeserializationSchema<T> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final Class<T> outputType;

  public DecodableRecordDeserializationSchema(Class<T> outputType) {
    this.outputType = outputType;
  }

  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<T> out)
      throws IOException {
    try {
      var rawKey = record.key();
      var rawValue = record.value();
      var jsonObjNode = OBJECT_MAPPER.createObjectNode();
      jsonObjNode.set(
          DEFAULT_KEY_FIELD_NAME,
          rawKey != null ? OBJECT_MAPPER.readTree(rawKey) : OBJECT_MAPPER.nullNode());
      jsonObjNode.set(
          DEFAULT_VALUE_FIELD_NAME,
          rawValue != null ? OBJECT_MAPPER.readTree(rawValue) : OBJECT_MAPPER.nullNode());
      out.collect(OBJECT_MAPPER.treeToValue(jsonObjNode, outputType));
    } catch (Exception e) {
      throw new IOException(
          String.format(
              "failed to deserialize record (key: %s, value: %s)",
              new String(record.key()), new String(record.value())),
          e);
    }
  }

  @Override
  public TypeInformation<T> getProducedType() {
    return Types.POJO(outputType);
  }

  @Override
  public void open(DeserializationSchema.InitializationContext context) throws Exception {
    KafkaRecordDeserializationSchema.super.open(context);
  }
}

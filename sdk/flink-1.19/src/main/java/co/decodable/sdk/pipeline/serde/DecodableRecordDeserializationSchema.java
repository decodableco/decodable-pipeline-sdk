/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.serde;

import static co.decodable.sdk.pipeline.DecodableAbstractStreamRecord.*;

import co.decodable.sdk.pipeline.DecodableAbstractStreamRecord;
import java.io.IOException;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public final class DecodableRecordDeserializationSchema<
        T extends DecodableAbstractStreamRecord<?, ?>>
    implements KafkaRecordDeserializationSchema<T> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final Class<T> outputType;

  public DecodableRecordDeserializationSchema(Class<T> outputType) {
    this.outputType = outputType;
  }

  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<T> out)
      throws IOException {
    var key = record.key();
    var value = record.value();
    var kvNode = OBJECT_MAPPER.createObjectNode();
    kvNode.set(
        KEY_FIELD_NAME, key != null ? OBJECT_MAPPER.readTree(key) : OBJECT_MAPPER.nullNode());
    kvNode.set(
        VALUE_FIELD_NAME, value != null ? OBJECT_MAPPER.readTree(value) : OBJECT_MAPPER.nullNode());
    out.collect(OBJECT_MAPPER.treeToValue(kvNode, outputType));
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

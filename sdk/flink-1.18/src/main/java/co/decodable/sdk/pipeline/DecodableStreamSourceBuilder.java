/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.serde.DecodableRecordDeserializationSchema;
import co.decodable.sdk.pipeline.util.Incubating;
import org.apache.flink.api.common.serialization.DeserializationSchema;

/** Builder for creating {@literal DecodableStreamSource} instances. */
@Incubating
public interface DecodableStreamSourceBuilder<T> {

  /** Specifies the name of the stream to read from. */
  DecodableStreamSourceBuilder<T> withStreamName(String streamName);

  /**
   * @deprecated Specifies the id of the stream to read from. Use {@link #withStreamName(String)}
   *     instead.
   */
  @Deprecated
  DecodableStreamSourceBuilder<T> withStreamId(String streamId);

  /** Specifies the start-up mode to use when reading from the stream. */
  DecodableStreamSourceBuilder<T> withStartupMode(StartupMode startupMode);

  /**
   * @deprecated Specifies the value only deserialization schema to be used. Use {@link
   *     #withRecordDeserializationSchema(DecodableRecordDeserializationSchema)} instead.
   */
  @Deprecated
  DecodableStreamSourceBuilder<T> withDeserializationSchema(
      DeserializationSchema<T> deserializationSchema);

  /**
   * Specifies the record deserialization schema to be used which supports both key and value parts
   * of a record.
   */
  DecodableStreamSourceBuilder<T> withRecordDeserializationSchema(
      DecodableRecordDeserializationSchema<?> recordDeserializationSchema);

  /** Returns a new {@link DecodableStreamSource} for the given configuration. */
  DecodableStreamSource<T> build();
}

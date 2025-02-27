/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import org.apache.flink.api.common.serialization.DeserializationSchema;

/** Builder for creating {@literal DecodableStreamSource} instances. */
@Incubating
public interface DecodableStreamSourceBuilder<T> {

  /**
   * Specifies the name of the stream to read from. Either this or {@link #withStreamId(String)} may
   * be used, but not both.
   */
  DecodableStreamSourceBuilder<T> withStreamName(String streamName);

  /**
   * Specifies the id of the stream to read from. Either this or {@link #withStreamName(String)} may
   * be used, but not both.
   */
  DecodableStreamSourceBuilder<T> withStreamId(String streamId);

  /** Specifies the start-up mode to use when reading from the stream. */
  DecodableStreamSourceBuilder<T> withStartupMode(StartupMode startupMode);

  /** Specifies the deserialization schema to be used. */
  DecodableStreamSourceBuilder<T> withDeserializationSchema(
      DeserializationSchema<T> deserializationSchema);

  /** Returns a new {@link DecodableStreamSource} for the given configuration. */
  DecodableStreamSource<T> build();
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/** Builder for creating {@literal DecodableStreamSource} instances. */
@Incubating
public interface DecodableStreamSourceBuilder<T> {

  DecodableStreamSourceBuilder<T> withStreamExecutionEnvironment(
      StreamExecutionEnvironment streamExecutionEnvironment);

  DecodableStreamSourceBuilder<T> withWatermarkStrategy(WatermarkStrategy<T> watermarkStrategy);

  /** Specifies the name of the stream to read from. */
  DecodableStreamSourceBuilder<T> withStreamName(String streamName);

  /** Specifies the start-up mode to use when reading from the stream. */
  DecodableStreamSourceBuilder<T> withStartupMode(StartupMode startupMode);

  /** Specifies the deserialization schema to be used. */
  DecodableStreamSourceBuilder<T> withDeserializationSchema(
      DeserializationSchema<T> deserializationSchema);

  DecodableStreamSourceBuilder<T> withName(String name);

  /**
   * Returns a new {@link DataStream} containing a {@link DecodableStreamSource} for the given
   * configuration.
   */
  DataStream<T> build();
}

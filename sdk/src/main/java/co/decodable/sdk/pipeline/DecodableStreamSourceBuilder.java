/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;

/** Builder for creating {@literal DecodableStreamSource} instances. */
@Incubating
public interface DecodableStreamSourceBuilder {

  /**
   * Specifies the name of the stream to read from. Either this or {@link #withStreamId(String)} may
   * be used, but not both.
   */
  DecodableStreamSourceBuilder withStreamName(String streamName);

  /**
   * Specifies the id of the stream to read from. Either this or {@link #withStreamName(String)} may
   * be used, but not both.
   */
  DecodableStreamSourceBuilder withStreamId(String streamId);

  /** Specifies the start-up mode to use when reading from the stream. */
  DecodableStreamSourceBuilder withStartupMode(StartupMode startupMode);

  /** Returns a new {@link DecodableStreamSource} for the given configuration. */
  DecodableStreamSource<String> build();
}

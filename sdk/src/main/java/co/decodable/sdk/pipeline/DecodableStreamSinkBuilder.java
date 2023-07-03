/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;

/** Builder for creating {@literal DecodableStreamSink} instances. */
@Incubating
public interface DecodableStreamSinkBuilder {

  /**
   * Specifies the name of the stream to write to. Either this or {@link #withStreamId(String)} may
   * be used, but not both.
   */
  DecodableStreamSinkBuilder withStreamName(String streamName);

  /**
   * Specifies the id of the stream to write to. Either this or {@link #withStreamName(String)} may
   * be used, but not both.
   */
  DecodableStreamSinkBuilder withStreamId(String streamId);

  /** Returns a new {@link DecodableStreamSink} for the given configuration. */
  DecodableStreamSink<String> build();
}

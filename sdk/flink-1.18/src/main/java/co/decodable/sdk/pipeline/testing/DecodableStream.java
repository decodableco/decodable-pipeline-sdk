/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.testing;

import co.decodable.sdk.pipeline.util.Incubating;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Represents a data stream on the Decodable platform.
 *
 * @param <T> The element type of this stream
 */
@Incubating
public interface DecodableStream<T> {

  /** Adds the given stream record to this stream. */
  void add(StreamRecord<T> streamRecord);

  /** Retrieves one element from this stream. */
  Future<StreamRecord<T>> takeOne();

  /** Retrieves {@code n} elements from this stream. */
  Future<List<StreamRecord<T>>> take(int n);
}

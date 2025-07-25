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
 * Represents a keyed data stream on the Decodable platform.
 *
 * @param <K> The key data type for a record in this stream
 * @param <V> The value data type for a record in this stream
 */
@Incubating
public interface KeyedDecodableStream<K, V> {

  /** Adds the given keyed stream record to this stream. */
  void add(KeyedStreamRecord<K, V> streamRecord);

  /** Retrieves one keyed record from this stream. */
  Future<KeyedStreamRecord<K, V>> takeOne();

  /** Retrieves {@code n} keyed records from this stream. */
  Future<List<KeyedStreamRecord<K, V>>> take(int n);
}

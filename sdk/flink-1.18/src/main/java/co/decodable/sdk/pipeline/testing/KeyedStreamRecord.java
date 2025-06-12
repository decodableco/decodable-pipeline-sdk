/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.testing;

import co.decodable.sdk.pipeline.util.Incubating;

/**
 * Represents one element on a keyed Decodable stream.
 *
 * @param <K> The key data type of this record
 * @param <V> The value data type of this record
 */
@Incubating
public class KeyedStreamRecord<K, V> {

  private final K key;
  private final V value;

  /** Creates a new stream record with the given value. */
  public KeyedStreamRecord(K key, V value) {
    this.key = key;
    this.value = value;
  }

  /** Returns the key of this keyed stream record. */
  public K key() {
    return key;
  }

  /** Returns the value of this keyed stream record. */
  public V value() {
    return value;
  }
}

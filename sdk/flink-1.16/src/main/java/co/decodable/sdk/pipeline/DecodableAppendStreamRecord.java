/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import java.util.Objects;

/**
 * Abstract base class to represent one record as a key-value pair in a Decodable append stream.
 *
 * @param <K> The key data type of this record
 * @param <V> The value data type of this record
 */
@Incubating
public abstract class DecodableAppendStreamRecord<K, V>
    implements DecodableKeyedStreamRecord<K, V> {

  private K key;
  private V value;

  public DecodableAppendStreamRecord() {}

  public DecodableAppendStreamRecord(V value) {
    this.key = null;
    this.value = value;
  }

  public DecodableAppendStreamRecord(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "DecodableAppendStreamRecord{" + "key=" + key + ", value=" + value + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DecodableAppendStreamRecord<?, ?> that = (DecodableAppendStreamRecord<?, ?>) o;
    return Objects.equals(key, that.key) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }
}

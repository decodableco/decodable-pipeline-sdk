/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import java.util.Objects;

/**
 * Abstract base class to represent one record in a Decodable stream as a key-value pair.
 *
 * @param <K> The key data type of this record
 * @param <V> The value data type of this record
 */
public abstract class DecodableAbstractStreamRecord<K, V>
    implements DecodableKeyedStreamRecord<K, V> {

  public static final String KEY_FIELD_NAME = "key";
  public static final String VALUE_FIELD_NAME = "value";

  private K key;
  private V value;

  public DecodableAbstractStreamRecord() {}

  public DecodableAbstractStreamRecord(V value) {
    this.key = null;
    this.value = value;
  }

  public DecodableAbstractStreamRecord(K key, V value) {
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
    return "DecodableAbstractStreamRecord{" + "key=" + key + ", value=" + value + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DecodableAbstractStreamRecord<?, ?> that = (DecodableAbstractStreamRecord<?, ?>) o;
    return Objects.equals(key, that.key) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }
}

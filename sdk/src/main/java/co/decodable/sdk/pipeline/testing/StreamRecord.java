/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.testing;

/**
 * Represents one element on a Decodable stream.
 *
 * @param <T> The data type of this record
 */
public class StreamRecord<T> {

  private final T value;

  /** Creates a new stream record with the given value. */
  public StreamRecord(T value) {
    this.value = value;
  }

  /** Returns the value of this stream record. */
  public T value() {
    return value;
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;

/**
 * Interface to represent one record in a Decodable stream as a key-value pair.
 *
 * @param <K> The key data type of this record
 * @param <V> The value data type of this record
 */
@Incubating
public interface DecodableKeyedStreamRecord<K, V> {

  String DEFAULT_KEY_FIELD_NAME = "key";
  String DEFAULT_VALUE_FIELD_NAME = "value";

  K getKey();

  V getValue();
}

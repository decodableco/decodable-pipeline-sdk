/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.internal;

import co.decodable.sdk.DecodableSourceEnumeratorState;
import org.apache.flink.connector.kafka.source.enumerator.KafkaSourceEnumState;

public class DecodableSourceEnumeratorStateImpl implements DecodableSourceEnumeratorState {

  private final KafkaSourceEnumState delegate;

  public DecodableSourceEnumeratorStateImpl(KafkaSourceEnumState delegate) {
    this.delegate = delegate;
  }

  public KafkaSourceEnumState getDelegate() {
    return delegate;
  }
}

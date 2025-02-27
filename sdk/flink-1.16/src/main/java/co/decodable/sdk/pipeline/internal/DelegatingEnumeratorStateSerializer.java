/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableSourceEnumeratorState;
import java.io.IOException;
import org.apache.flink.connector.kafka.source.enumerator.KafkaSourceEnumState;
import org.apache.flink.core.io.SimpleVersionedSerializer;

public class DelegatingEnumeratorStateSerializer
    implements SimpleVersionedSerializer<DecodableSourceEnumeratorState> {

  private final SimpleVersionedSerializer<KafkaSourceEnumState> delegate;

  public DelegatingEnumeratorStateSerializer(
      SimpleVersionedSerializer<KafkaSourceEnumState> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getVersion() {
    return delegate.getVersion();
  }

  @Override
  public byte[] serialize(DecodableSourceEnumeratorState obj) throws IOException {
    return delegate.serialize(((DecodableSourceEnumeratorStateImpl) obj).getDelegate());
  }

  @Override
  public DecodableSourceEnumeratorState deserialize(int version, byte[] serialized)
      throws IOException {
    return new DecodableSourceEnumeratorStateImpl(delegate.deserialize(version, serialized));
  }
}

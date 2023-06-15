/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableWriterState;
import java.io.IOException;
import org.apache.flink.connector.kafka.source.enumerator.KafkaSourceEnumState;
import org.apache.flink.core.io.SimpleVersionedSerializer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DelegatingWriterStateSerializer
    implements SimpleVersionedSerializer<DecodableWriterState> {

  // Can't use the Kafka sink's implementation, as it exposes non-public types in its signatures
  private final SimpleVersionedSerializer delegate;

  public DelegatingWriterStateSerializer(SimpleVersionedSerializer<KafkaSourceEnumState> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getVersion() {
    return delegate.getVersion();
  }

  @Override
  public byte[] serialize(DecodableWriterState obj) throws IOException {
    return delegate.serialize(((DecodableWriterStateImpl) obj).getDelegate());
  }

  @Override
  public DecodableWriterState deserialize(int version, byte[] serialized) throws IOException {
    return new DecodableWriterStateImpl(delegate.deserialize(version, serialized));
  }
}

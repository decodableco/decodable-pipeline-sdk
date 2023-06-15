/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableCommittable;
import java.io.IOException;
import org.apache.flink.core.io.SimpleVersionedSerializer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DelegatingCommittableSerializer
    implements SimpleVersionedSerializer<DecodableCommittable> {

  // Can't use the Kafka sink's implementation, as it exposes non-public types in its signatures
  private final SimpleVersionedSerializer delegate;

  public DelegatingCommittableSerializer(SimpleVersionedSerializer<?> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getVersion() {
    return delegate.getVersion();
  }

  @Override
  public byte[] serialize(DecodableCommittable obj) throws IOException {
    return delegate.serialize(((DecodableCommittableImpl) obj).getDelegate());
  }

  @Override
  public DecodableCommittable deserialize(int version, byte[] serialized) throws IOException {
    return new DecodableCommittableImpl(delegate.deserialize(version, serialized));
  }
}

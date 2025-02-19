/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableSourceSplit;
import java.io.IOException;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;
import org.apache.flink.core.io.SimpleVersionedSerializer;

public class DelegatingSplitSerializer implements SimpleVersionedSerializer<DecodableSourceSplit> {

  private final SimpleVersionedSerializer<KafkaPartitionSplit> delegate;

  public DelegatingSplitSerializer(SimpleVersionedSerializer<KafkaPartitionSplit> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getVersion() {
    return delegate.getVersion();
  }

  @Override
  public byte[] serialize(DecodableSourceSplit obj) throws IOException {
    return delegate.serialize(((DecodableSourceSplitImpl) obj).getDelegate());
  }

  @Override
  public DecodableSourceSplit deserialize(int version, byte[] serialized) throws IOException {
    return new DecodableSourceSplitImpl(delegate.deserialize(version, serialized));
  }
}

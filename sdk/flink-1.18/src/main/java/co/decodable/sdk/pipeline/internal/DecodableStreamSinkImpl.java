/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableStreamSink;
import co.decodable.sdk.pipeline.DecodableWriter;
import java.io.IOException;
import java.util.Collection;
import org.apache.flink.api.connector.sink2.Committer;
import org.apache.flink.api.connector.sink2.StatefulSink;
import org.apache.flink.api.connector.sink2.TwoPhaseCommittingSink;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.core.io.SimpleVersionedSerializer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DecodableStreamSinkImpl<T> implements DecodableStreamSink<T> {

  private static final long serialVersionUID = 3654512984006560177L;

  // Can't use KafkaSink, as it exposes non-public types in its signatures
  private final StatefulSink delegate;

  public DecodableStreamSinkImpl(KafkaSink<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public DecodableWriter<T> createWriter(InitContext context) throws IOException {
    return new DelegatingStatefulSinkWriter<T>(
        (StatefulSinkWriter<T, ?>) delegate.createWriter(context));
  }

  @Override
  public StatefulSinkWriter<T, Object> restoreWriter(
      InitContext context, Collection<Object> recoveredState) throws IOException {

    return delegate.restoreWriter(context, recoveredState);
  }

  @Override
  public SimpleVersionedSerializer<Object> getWriterStateSerializer() {
    return delegate.getWriterStateSerializer();
  }

  @Override
  public Committer<Object> createCommitter() throws IOException {
    return ((TwoPhaseCommittingSink) delegate).createCommitter();
  }

  @Override
  public SimpleVersionedSerializer<Object> getCommittableSerializer() {
    return ((TwoPhaseCommittingSink) delegate).getCommittableSerializer();
  }
}

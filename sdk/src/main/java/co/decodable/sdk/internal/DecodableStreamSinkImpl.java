/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.internal;

import co.decodable.sdk.DecodableCommittable;
import co.decodable.sdk.DecodableStreamSink;
import co.decodable.sdk.DecodableWriter;
import co.decodable.sdk.DecodableWriterState;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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
    return new DelegatingStatefulSinkWriter<T>(delegate.createWriter(context));
  }

  @Override
  public StatefulSinkWriter<T, DecodableWriterState> restoreWriter(
      InitContext context, Collection<DecodableWriterState> recoveredState) throws IOException {

    List<Object> kafkaState =
        recoveredState.stream()
            .map(s -> ((DecodableWriterStateImpl) s).getDelegate())
            .collect(Collectors.toList());

    return delegate.restoreWriter(context, kafkaState);
  }

  @Override
  public SimpleVersionedSerializer<DecodableWriterState> getWriterStateSerializer() {
    return new DelegatingWriterStateSerializer(delegate.getWriterStateSerializer());
  }

  @Override
  public Committer<DecodableCommittable> createCommitter() throws IOException {
    return new DelegatingCommitter(((TwoPhaseCommittingSink) delegate).createCommitter());
  }

  @Override
  public SimpleVersionedSerializer<DecodableCommittable> getCommittableSerializer() {
    return new DelegatingCommittableSerializer(
        ((TwoPhaseCommittingSink) delegate).getCommittableSerializer());
  }
}

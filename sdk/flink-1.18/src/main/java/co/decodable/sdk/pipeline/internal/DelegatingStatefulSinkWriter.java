/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.flink.api.connector.sink2.StatefulSink.StatefulSinkWriter;
import org.apache.flink.api.connector.sink2.TwoPhaseCommittingSink;

@SuppressWarnings("unchecked")
public class DelegatingStatefulSinkWriter<T> implements DecodableWriter<T> {

  // Can't use the Kafka sink's implementation, as it exposes non-public types in its signatures
  private final StatefulSinkWriter delegate;

  public DelegatingStatefulSinkWriter(StatefulSinkWriter<T, ?> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void write(T element, Context context) throws IOException, InterruptedException {
    delegate.write(element, context);
  }

  @Override
  public void flush(boolean endOfInput) throws IOException, InterruptedException {
    delegate.flush(endOfInput);
  }

  @Override
  public void close() throws Exception {
    delegate.close();
  }

  @Override
  public List<Object> snapshotState(long checkpointId) throws IOException {
    return delegate.snapshotState(checkpointId);
  }

  @Override
  public Collection<Object> prepareCommit() throws IOException, InterruptedException {
    return ((TwoPhaseCommittingSink.PrecommittingSinkWriter) delegate).prepareCommit();
  }
}

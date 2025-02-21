/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableSourceSplit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.flink.api.connector.source.ReaderOutput;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;
import org.apache.flink.core.io.InputStatus;

public class DelegatingSourceReader<T> implements SourceReader<T, DecodableSourceSplit> {

  private final SourceReader<T, KafkaPartitionSplit> delegate;

  public DelegatingSourceReader(SourceReader<T, KafkaPartitionSplit> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void close() throws Exception {
    delegate.close();
  }

  @Override
  public void start() {
    delegate.start();
  }

  @Override
  public InputStatus pollNext(ReaderOutput<T> output) throws Exception {
    return delegate.pollNext(output);
  }

  @Override
  public List<DecodableSourceSplit> snapshotState(long checkpointId) {
    return delegate.snapshotState(checkpointId).stream()
        .map(DecodableSourceSplitImpl::new)
        .collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<Void> isAvailable() {
    return delegate.isAvailable();
  }

  @Override
  public void addSplits(List<DecodableSourceSplit> splits) {
    List<KafkaPartitionSplit> delegateSplits =
        splits.stream()
            .map(s -> ((DecodableSourceSplitImpl) s).getDelegate())
            .collect(Collectors.toList());

    delegate.addSplits(delegateSplits);
  }

  @Override
  public void notifyNoMoreSplits() {
    delegate.notifyNoMoreSplits();
  }
}

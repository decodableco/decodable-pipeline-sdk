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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.flink.api.connector.source.ReaderInfo;
import org.apache.flink.api.connector.source.SourceEvent;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.api.connector.source.SplitsAssignment;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;
import org.apache.flink.metrics.groups.SplitEnumeratorMetricGroup;

public class DelegatingSplitEnumeratorContext
    implements SplitEnumeratorContext<KafkaPartitionSplit> {

  private final SplitEnumeratorContext<DecodableSourceSplit> delegate;

  public DelegatingSplitEnumeratorContext(SplitEnumeratorContext<DecodableSourceSplit> delegate) {
    this.delegate = delegate;
  }

  @Override
  public SplitEnumeratorMetricGroup metricGroup() {
    return delegate.metricGroup();
  }

  @Override
  public void sendEventToSourceReader(int subtaskId, SourceEvent event) {
    delegate.sendEventToSourceReader(subtaskId, event);
  }

  @Override
  public int currentParallelism() {
    return delegate.currentParallelism();
  }

  @Override
  public Map<Integer, ReaderInfo> registeredReaders() {
    return delegate.registeredReaders();
  }

  @Override
  public void assignSplits(SplitsAssignment<KafkaPartitionSplit> newSplitAssignments) {
    Map<Integer, List<DecodableSourceSplit>> delegateAssignments =
        newSplitAssignments.assignment().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> e.getKey(),
                    e ->
                        e.getValue().stream()
                            .map(DecodableSourceSplitImpl::new)
                            .collect(Collectors.toList())));

    delegate.assignSplits(new SplitsAssignment<DecodableSourceSplit>(delegateAssignments));
  }

  @Override
  public void signalNoMoreSplits(int subtask) {
    delegate.signalNoMoreSplits(subtask);
  }

  @Override
  public <T> void callAsync(Callable<T> callable, BiConsumer<T, Throwable> handler) {
    delegate.callAsync(callable, handler);
  }

  @Override
  public <T> void callAsync(
      Callable<T> callable,
      BiConsumer<T, Throwable> handler,
      long initialDelayMillis,
      long periodMillis) {
    delegate.callAsync(callable, handler, initialDelayMillis, periodMillis);
  }

  @Override
  public void runInCoordinatorThread(Runnable runnable) {
    delegate.runInCoordinatorThread(runnable);
  }
}

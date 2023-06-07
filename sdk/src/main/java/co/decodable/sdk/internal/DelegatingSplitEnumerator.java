/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.internal;

import co.decodable.sdk.DecodableSourceEnumeratorState;
import co.decodable.sdk.DecodableSourceSplit;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.connector.kafka.source.enumerator.KafkaSourceEnumState;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;

public class DelegatingSplitEnumerator
    implements SplitEnumerator<DecodableSourceSplit, DecodableSourceEnumeratorState> {

  private final SplitEnumerator<KafkaPartitionSplit, KafkaSourceEnumState> delegate;

  public DelegatingSplitEnumerator(
      SplitEnumerator<KafkaPartitionSplit, KafkaSourceEnumState> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void start() {
    delegate.start();
  }

  @Override
  public void handleSplitRequest(int subtaskId, String requesterHostname) {
    delegate.handleSplitRequest(subtaskId, requesterHostname);
  }

  @Override
  public void addSplitsBack(List<DecodableSourceSplit> splits, int subtaskId) {
    List<KafkaPartitionSplit> delegateSplits =
        splits.stream()
            .map(s -> ((DecodableSourceSplitImpl) s).getDelegate())
            .collect(Collectors.toList());

    delegate.addSplitsBack(delegateSplits, subtaskId);
  }

  @Override
  public void addReader(int subtaskId) {
    delegate.addReader(subtaskId);
  }

  @Override
  public DecodableSourceEnumeratorState snapshotState(long checkpointId) throws Exception {
    return new DecodableSourceEnumeratorStateImpl(delegate.snapshotState(checkpointId));
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }
}

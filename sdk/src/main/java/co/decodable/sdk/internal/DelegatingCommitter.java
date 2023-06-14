/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.internal;

import co.decodable.sdk.DecodableCommittable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.flink.api.connector.sink2.Committer;
import org.apache.flink.streaming.runtime.operators.sink.committables.CommitRequestImpl;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DelegatingCommitter implements Committer<DecodableCommittable> {

  // Can't use the Kafka sink's implementation, as it exposes non-public types in its signatures
  private Committer delegate;

  public DelegatingCommitter(Committer<?> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void close() throws Exception {
    delegate.close();
  }

  @Override
  public void commit(Collection<CommitRequest<DecodableCommittable>> committables)
      throws IOException, InterruptedException {

    List<CommitRequestImplExt> delegateCommittables =
        committables.stream()
            .map(
                c ->
                    new CommitRequestImplExt(
                        ((DecodableCommittableImpl) c.getCommittable()).getDelegate()))
            .collect(Collectors.toList());

    delegate.commit(delegateCommittables);
  }

  private static class CommitRequestImplExt<T> extends CommitRequestImpl<T> {

    protected CommitRequestImplExt(T committable) {
      super(committable);
    }
  }
}

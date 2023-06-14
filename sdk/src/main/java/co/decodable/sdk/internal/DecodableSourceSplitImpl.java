/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.internal;

import co.decodable.sdk.DecodableSourceSplit;
import org.apache.flink.connector.kafka.source.split.KafkaPartitionSplit;

public class DecodableSourceSplitImpl implements DecodableSourceSplit {

  private final KafkaPartitionSplit delegate;

  public DecodableSourceSplitImpl(KafkaPartitionSplit delegate) {
    this.delegate = delegate;
  }

  @Override
  public String splitId() {
    return delegate.splitId();
  }

  public KafkaPartitionSplit getDelegate() {
    return delegate;
  }
}

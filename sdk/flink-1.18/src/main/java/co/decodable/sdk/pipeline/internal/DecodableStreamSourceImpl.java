/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableSourceEnumeratorState;
import co.decodable.sdk.pipeline.DecodableSourceSplit;
import co.decodable.sdk.pipeline.DecodableStreamSource;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.core.io.SimpleVersionedSerializer;

class DecodableStreamSourceImpl<T> implements DecodableStreamSource<T> {

  private static final long serialVersionUID = 7762732921098678433L;

  private final KafkaSource<T> delegate;

  DecodableStreamSourceImpl(KafkaSource<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Boundedness getBoundedness() {
    return delegate.getBoundedness();
  }

  @Override
  public SourceReader<T, DecodableSourceSplit> createReader(SourceReaderContext readerContext)
      throws Exception {
    return new DelegatingSourceReader<T>(delegate.createReader(readerContext));
  }

  @Override
  public SplitEnumerator<DecodableSourceSplit, DecodableSourceEnumeratorState> createEnumerator(
      SplitEnumeratorContext<DecodableSourceSplit> enumContext) throws Exception {

    return new DelegatingSplitEnumerator(
        delegate.createEnumerator(new DelegatingSplitEnumeratorContext(enumContext)));
  }

  @Override
  public SplitEnumerator<DecodableSourceSplit, DecodableSourceEnumeratorState> restoreEnumerator(
      SplitEnumeratorContext<DecodableSourceSplit> enumContext,
      DecodableSourceEnumeratorState checkpoint)
      throws Exception {
    return new DelegatingSplitEnumerator(
        delegate.restoreEnumerator(
            new DelegatingSplitEnumeratorContext(enumContext),
            ((DecodableSourceEnumeratorStateImpl) checkpoint).getDelegate()));
  }

  @Override
  public SimpleVersionedSerializer<DecodableSourceSplit> getSplitSerializer() {
    return new DelegatingSplitSerializer(delegate.getSplitSerializer());
  }

  @Override
  public SimpleVersionedSerializer<DecodableSourceEnumeratorState>
      getEnumeratorCheckpointSerializer() {
    return new DelegatingEnumeratorStateSerializer(delegate.getEnumeratorCheckpointSerializer());
  }

  @Override
  public TypeInformation<T> getProducedType() {
    return delegate.getProducedType();
  }
}

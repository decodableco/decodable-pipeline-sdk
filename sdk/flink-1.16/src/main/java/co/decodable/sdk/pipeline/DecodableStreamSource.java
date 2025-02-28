/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.internal.DecodableStreamSourceBuilderImpl;
import co.decodable.sdk.pipeline.util.Incubating;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;

/**
 * A {@link Source} which allows to retrieve the contents of a <a
 * href="https://docs.decodable.co/docs/streams">Decodable stream</a> from within a Flink job. The
 * stream must be referenced in {@link co.decodable.sdk.pipeline.metadata.SourceStreams} to be
 * accessible.
 *
 * @param <T> The data type of this stream
 */
@Incubating
public interface DecodableStreamSource<T>
    extends Source<T, DecodableSourceSplit, DecodableSourceEnumeratorState>,
        ResultTypeQueryable<T> {

  /** Returns a builder for creating a new {@link DecodableStreamSource}. */
  public static <T> DecodableStreamSourceBuilder<T> builder() {
    return new DecodableStreamSourceBuilderImpl<T>();
  }
}

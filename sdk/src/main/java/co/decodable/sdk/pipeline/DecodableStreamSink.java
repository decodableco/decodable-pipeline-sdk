/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.internal.DecodableStreamSinkBuilderImpl;
import co.decodable.sdk.pipeline.util.Incubating;
import java.io.IOException;
import org.apache.flink.api.connector.sink2.StatefulSink;
import org.apache.flink.api.connector.sink2.TwoPhaseCommittingSink;

/**
 * A {@link StatefulSink} which allows to write to a <a
 * href="https://docs.decodable.co/docs/streams">Decodable stream</a> from within a Flink job.
 *
 * @param <T> The data type of this stream
 */
@Incubating
public interface DecodableStreamSink<T>
    extends StatefulSink<T, DecodableWriterState>, TwoPhaseCommittingSink<T, DecodableCommittable> {

  /** Returns a builder for creating a new {@link DecodableStreamSink}. */
  public static DecodableStreamSinkBuilder builder() {
    return new DecodableStreamSinkBuilderImpl();
  }

  /** {@inheritDoc} */
  @Override
  DecodableWriter<T> createWriter(InitContext context) throws IOException;
}

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

@Incubating
public interface DecodableStreamSink<T>
    extends StatefulSink<T, DecodableWriterState>, TwoPhaseCommittingSink<T, DecodableCommittable> {

  public static DecodableStreamSinkBuilder builder() {
    return new DecodableStreamSinkBuilderImpl();
  }

  DecodableWriter<T> createWriter(InitContext context) throws IOException;
}

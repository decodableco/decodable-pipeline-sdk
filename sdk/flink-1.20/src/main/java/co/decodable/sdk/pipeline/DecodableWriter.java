/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import org.apache.flink.api.connector.sink2.StatefulSink;
import org.apache.flink.api.connector.sink2.TwoPhaseCommittingSink;

/**
 * Sink writer used by {@link DecodableStreamSink}.
 *
 * @param <T> Data type of the writer
 */
@Incubating
public interface DecodableWriter<T>
    extends StatefulSink.StatefulSinkWriter<T, Object>,
        TwoPhaseCommittingSink.PrecommittingSinkWriter<T, Object> {}

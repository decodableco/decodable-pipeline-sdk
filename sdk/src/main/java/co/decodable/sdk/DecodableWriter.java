/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk;

import co.decodable.sdk.util.Incubating;
import org.apache.flink.api.connector.sink2.StatefulSink;
import org.apache.flink.api.connector.sink2.TwoPhaseCommittingSink;

@Incubating
public interface DecodableWriter<T>
    extends StatefulSink.StatefulSinkWriter<T, DecodableWriterState>,
        TwoPhaseCommittingSink.PrecommittingSinkWriter<T, DecodableCommittable> {}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk;

import co.decodable.sdk.internal.DecodableStreamSourceBuilderImpl;
import co.decodable.sdk.util.Incubating;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;

@Incubating
public interface DecodableStreamSource<T>
    extends Source<T, DecodableSourceSplit, DecodableSourceEnumeratorState>,
        ResultTypeQueryable<T> {

  public static DecodableStreamSourceBuilder builder() {
    return new DecodableStreamSourceBuilderImpl();
  }
}

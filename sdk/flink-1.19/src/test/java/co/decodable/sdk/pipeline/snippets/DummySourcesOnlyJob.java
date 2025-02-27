/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.snippets;

import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams({DummySourcesOnlyJob.SOURCE1, DummySourcesOnlyJob.SOURCE2})
public class DummySourcesOnlyJob {
  public static final String SOURCE1 = "source1";
  public static final String SOURCE2 = "source2";
}

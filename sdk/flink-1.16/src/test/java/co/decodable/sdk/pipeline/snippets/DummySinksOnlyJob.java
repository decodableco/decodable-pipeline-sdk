/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.snippets;

import co.decodable.sdk.pipeline.metadata.SinkStreams;

@SinkStreams({DummySinksOnlyJob.SINK1, DummySinksOnlyJob.SINK2})
public class DummySinksOnlyJob {
  public static final String SINK1 = "sink1";
  public static final String SINK2 = "sink2";
}

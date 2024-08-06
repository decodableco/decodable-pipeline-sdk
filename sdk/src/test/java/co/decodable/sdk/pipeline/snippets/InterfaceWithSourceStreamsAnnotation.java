/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.snippets;

import static co.decodable.sdk.pipeline.snippets.InterfaceWithSourceStreamsAnnotation.STREAM_NAME;

import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams(STREAM_NAME)
public interface InterfaceWithSourceStreamsAnnotation {
  String STREAM_NAME = "stream-name";
}

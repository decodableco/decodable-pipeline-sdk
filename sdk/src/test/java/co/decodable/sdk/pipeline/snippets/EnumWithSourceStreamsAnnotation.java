/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.snippets;

import co.decodable.sdk.pipeline.metadata.SourceStreams;

@SourceStreams(EnumWithSourceStreamsAnnotation.STREAM_NAME)
public enum EnumWithSourceStreamsAnnotation {
  ENUM_VALUE;

  static final String STREAM_NAME = "stream-name";
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;

/**
 * Defines from which offset to consume the underlying stream when starting up a {@link
 * DecodableStreamSource}.
 */
@Incubating
public enum StartupMode {
  /** Consume the stream starting at the earliest available offset. */
  EARLIEST_OFFSET,
  /** Consume the stream starting at the latest available offset. */
  LATEST_OFFSET;

  /** Parses the given string value into {@link StartupMode} instance. */
  public static StartupMode fromString(String value) {
    if (value == null) {
      return null;
    } else if ("earliest-offset".equals(value)) {
      return StartupMode.EARLIEST_OFFSET;
    } else if ("latest-offset".equals(value)) {
      return StartupMode.LATEST_OFFSET;
    } else {
      throw new IllegalArgumentException("Unsupported startup mode: " + value);
    }
  }
}

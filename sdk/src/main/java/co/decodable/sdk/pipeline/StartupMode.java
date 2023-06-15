/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;

@Incubating
public enum StartupMode {
  EARLIEST_OFFSET,
  LATEST_OFFSET;

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

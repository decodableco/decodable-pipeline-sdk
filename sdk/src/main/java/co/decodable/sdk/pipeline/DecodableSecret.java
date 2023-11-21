/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.internal.DecodableSecretImpl;
import java.time.Instant;

public interface DecodableSecret {
  String value();

  String name();

  String description();

  Instant createTime();

  Instant updateTime();

  static DecodableSecret withName(String name) {
    return new DecodableSecretImpl(name);
  }
}

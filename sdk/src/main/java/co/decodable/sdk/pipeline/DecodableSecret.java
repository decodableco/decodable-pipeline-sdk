/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.exception.SecretNotFoundException;
import co.decodable.sdk.pipeline.internal.DecodableSecretImpl;
import co.decodable.sdk.pipeline.util.Incubating;
import java.time.Instant;

/**
 * Represents a <a href="https://docs.decodable.co/docs/manage-secrets">Decodable secret</a>.
 * Exposes both metadata and the secret value. Create an instance and access its properties like
 * this:
 *
 * <p>{@snippet : var mySecret = DecodableSecret.withName("my-secret"); var mySecretValue =
 * mySecret.value(); doStuffWithMySecret(mySecretValue); }
 */
@Incubating
public interface DecodableSecret {
  /** Returns the plaintext secret value. */
  String value();

  /** Returns the name of the secret. */
  String name();

  /** Returns the secret description. */
  String description();

  /** Returns the creation time of the secret. */
  Instant createTime();

  /** Returns the time the secret was last updated. */
  Instant updateTime();

  /**
   * Looks up the secret by name and returns a {@link DecodableSecret} instance.
   *
   * @throws SecretNotFoundException if the secret is not present or cannot be read
   */
  static DecodableSecret withName(String name) throws SecretNotFoundException {
    return new DecodableSecretImpl(name);
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.exception;

/**
 * This exception is thrown when a <a href="https://docs.decodable.co/docs/manage-secrets">Decodable
 * secret</a> cannot be accessed. The most probable cause is that it was not referenced in the
 * pipeline's properties.
 */
public class SecretNotFoundException extends RuntimeException {
  public SecretNotFoundException(String message) {
    super(message);
  }

  public SecretNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

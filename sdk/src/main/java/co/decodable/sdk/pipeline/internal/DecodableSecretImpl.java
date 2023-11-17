/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableSecret;
import co.decodable.sdk.pipeline.exception.SecretNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

public class DecodableSecretImpl implements DecodableSecret {
  private static final String SECRET_DIRECTORY = "/opt/flink/opt/secrets/";

  private final String name;
  private final String value;

  public DecodableSecretImpl(String name) {
    this(name, SECRET_DIRECTORY);
  }

  DecodableSecretImpl(String name, String secretDirectory) {
    this.name = name;
    if (!secretDirectory.endsWith("/")) {
      secretDirectory = secretDirectory + "/";
    }
    var secretFile = new File(secretDirectory + name);
    if (!secretFile.exists()) {
      throw new SecretNotFoundException(
          String.format(
              "Secret [%s] not found. Please make sure it is included in this pipeline's properties.",
              name));
    }
    try {
      this.value = FileUtils.readFileToString(secretFile, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new SecretNotFoundException(
          String.format("Could not read secret [%s]. Please contact Decodable support.", name), e);
    }
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String getName() {
    return name;
  }
}

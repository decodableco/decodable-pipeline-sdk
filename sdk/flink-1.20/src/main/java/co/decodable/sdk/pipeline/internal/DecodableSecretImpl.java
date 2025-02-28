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
import co.decodable.sdk.pipeline.util.VisibleForTesting;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class DecodableSecretImpl implements DecodableSecret {

  private static final Path SECRET_DIRECTORY = Path.of("/opt/pipeline-secrets/");

  /** Will be removed once the backend doesn't use that path any more */
  @Deprecated(forRemoval = true)
  private static final Path LEGACY_SECRET_DIRECTORY = Path.of("/opt/flink/opt/secrets/");

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final String value;
  private final SecretMetadata metadata;

  public DecodableSecretImpl(String name) {
    this(name, SECRET_DIRECTORY, LEGACY_SECRET_DIRECTORY);
  }

  @VisibleForTesting
  DecodableSecretImpl(String name, Path secretsDirectory, Path fallbackDirectory) {
    var secret = getSecret(secretsDirectory, name, false);
    if (secret == null) {
      secret = getSecret(fallbackDirectory, name, true);
    }

    this.value = secret.value;
    this.metadata = secret.metadata;
  }

  private static SecretDescriptor getSecret(
      Path secretsDirectory, String secretName, boolean assertExistence) {
    var secretFile = secretsDirectory.resolve(secretName);
    var secretMetadataFile = secretsDirectory.resolve(secretName + ".metadata");
    if (!Files.exists(secretFile) || !Files.exists(secretMetadataFile)) {
      if (assertExistence) {
        throw new SecretNotFoundException(
            String.format(
                "Secret [%s] not found. Please make sure it is included in this pipeline's properties.",
                secretName));
      } else {
        return null;
      }
    }

    try {
      var value = Files.readString(secretFile, StandardCharsets.UTF_8);
      var metadata = objectMapper.readValue(secretMetadataFile.toFile(), SecretMetadata.class);

      return new SecretDescriptor(value, metadata);
    } catch (IOException e) {
      throw new SecretNotFoundException(
          String.format(
              "Could not read secret [%s]. Please contact Decodable support.", secretName),
          e);
    }
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public String name() {
    return metadata.name;
  }

  @Override
  public String description() {
    return metadata.description;
  }

  @Override
  public Instant createTime() {
    return metadata.createTime;
  }

  @Override
  public Instant updateTime() {
    return metadata.updateTime;
  }

  private static class SecretDescriptor {
    String value;
    SecretMetadata metadata;

    public SecretDescriptor(String value, SecretMetadata metadata) {
      this.value = value;
      this.metadata = metadata;
    }
  }

  private static class SecretMetadata {
    private final String name;
    private final String description;
    private final Instant createTime;
    private final Instant updateTime;

    public SecretMetadata(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("create_time") Long createTime,
        @JsonProperty("update_time") Long updateTime) {
      this.name = name;
      this.description = description;
      this.createTime = createTime == null ? null : Instant.ofEpochMilli(createTime);
      this.updateTime = updateTime == null ? null : Instant.ofEpochMilli(updateTime);
    }
  }
}

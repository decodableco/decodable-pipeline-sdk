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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.io.FileUtils;

public class DecodableSecretImpl implements DecodableSecret {
  private static final String SECRET_DIRECTORY = "/opt/flink/opt/secrets/";

  private final SecretMetadata metadata;
  private final String value;

  public DecodableSecretImpl(String name) {
    this(name, SECRET_DIRECTORY);
  }

  DecodableSecretImpl(String name, String secretDirectory) {
    if (!secretDirectory.endsWith("/")) {
      secretDirectory = secretDirectory + "/";
    }
    var secretFile = new File(secretDirectory + name);
    var secretMetadataFile = new File(secretDirectory + name + ".metadata");
    if (!secretFile.exists() || !secretMetadataFile.exists()) {
      throw new SecretNotFoundException(
          String.format(
              "Secret [%s] not found. Please make sure it is included in this pipeline's properties.",
              name));
    }
    try {
      this.value = FileUtils.readFileToString(secretFile, StandardCharsets.UTF_8);
      var objectMapper = new ObjectMapper();
      this.metadata = objectMapper.readValue(secretMetadataFile, SecretMetadata.class);
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
    return metadata.name;
  }

  @Override
  public String getDescription() {
    return metadata.description;
  }

  @Override
  public Date getCreateTime() {
    return metadata.createTime;
  }

  @Override
  public Date getUpdateTime() {
    return metadata.updateTime;
  }

  private static class SecretMetadata {
    private final String name;
    private final String description;
    private final Date createTime;
    private final Date updateTime;

    public SecretMetadata(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("create_time") Long createTime,
        @JsonProperty("update_time") Long updateTime) {
      this.name = name;
      this.description = description;
      this.createTime = createTime == null ? null : new Date(createTime);
      this.updateTime = updateTime == null ? null : new Date(updateTime);
    }
  }
}

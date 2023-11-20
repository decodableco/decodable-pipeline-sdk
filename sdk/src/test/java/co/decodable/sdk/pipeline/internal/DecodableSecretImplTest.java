/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import static org.assertj.core.api.Assertions.*;

import co.decodable.sdk.pipeline.exception.SecretNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class DecodableSecretImplTest {

  @Test
  public void fileExists() throws IOException {
    var secretFile = Files.createTempFile("secret", "");
    var metadataPath = secretFile.toAbsolutePath() + ".metadata";
    var secretMetadataFile = Files.createFile(Path.of(metadataPath));
    long createEpoch = 1700503745;
    var createTime = Date.from(Instant.ofEpochMilli(createEpoch));
    long updateEpoch = 1700503746;
    var updateTime = Date.from(Instant.ofEpochMilli(updateEpoch));
    Files.write(secretFile, "my-secret-value".getBytes(StandardCharsets.UTF_8));
    Files.write(
        secretMetadataFile,
        String.format(
                "{\"name\":\"%s\",\"description\":\"My secret\",\"create_time\":\"%d\",\"update_time\":\"%d\"}",
                secretMetadataFile.getFileName().toString(), createEpoch, updateEpoch)
            .getBytes(StandardCharsets.UTF_8));
    var secret =
        new DecodableSecretImpl(
            secretFile.getFileName().toString(), secretFile.getParent().toString());
    assertThat(secret.getValue()).isEqualTo("my-secret-value");
    assertThat(secret.getName()).startsWith("secret");
    assertThat(secret.getDescription()).isEqualTo("My secret");
    assertThat(secret.getCreateTime()).isEqualTo(createTime);
    assertThat(secret.getUpdateTime()).isEqualTo(updateTime);
  }

  @Test
  public void fileDoesNotExist() {
    assertThatThrownBy(() -> new DecodableSecretImpl("does-not-exist"))
        .isInstanceOf(SecretNotFoundException.class)
        .hasMessageContaining("Secret [does-not-exist] not found");
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.decodable.sdk.pipeline.exception.SecretNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

public class DecodableSecretImplTest {

  @Test
  public void fileExists() throws IOException {
    var secretFile = Files.createTempFile("secret", "");
    Files.write(secretFile, "my-secret-value".getBytes(StandardCharsets.UTF_8));
    var secret =
        new DecodableSecretImpl(
            secretFile.getFileName().toString(), secretFile.getParent().toString());
    assertThat(secret.getName()).startsWith("secret");
    assertThat(secret.getValue()).isEqualTo("my-secret-value");
  }

  @Test
  public void fileDoesNotExist() {
    assertThatThrownBy(() -> new DecodableSecretImpl("does-not-exist"))
        .isInstanceOf(SecretNotFoundException.class)
        .hasMessageContaining("Secret [does-not-exist] not found");
  }
}

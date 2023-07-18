/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal.config.metadata;

import static com.google.testing.compile.CompilationSubject.assertThat;

import co.decodable.sdk.pipeline.internal.metadata.MetadataProcessor;
import com.google.common.io.CharSource;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

public class MetadataProcessorTest {

  @Test
  public void shouldGenerateStreamNamesFile() throws MalformedURLException {
    URL jobFile =
        new File(
                "./src/test/java/co/decodable/sdk/pipeline/snippets/PurchaseOrderProcessingJob.java")
            .toURI()
            .toURL();

    Compilation compilation =
        Compiler.javac()
            .withProcessors(new MetadataProcessor())
            .compile(JavaFileObjects.forResource(jobFile));

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/decodable/stream-names.properties")
        .hasContents(
            CharSource.wrap(
                    "source-streams=purchase-orders\nsink-streams=purchase-orders-processed\n")
                .asByteSource(Charset.forName("UTF-8")));
  }
}

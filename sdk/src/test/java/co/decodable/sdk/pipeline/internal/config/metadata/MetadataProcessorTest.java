/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal.config.metadata;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import co.decodable.sdk.pipeline.internal.metadata.MetadataProcessor;
import com.google.common.io.CharSource;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

public class MetadataProcessorTest {

  private static final String OUTPUT_PATH = "META-INF/decodable/stream-names.properties";

  @Test
  public void shouldGenerateStreamNamesFile() throws MalformedURLException {
    URL jobFile =
        new File(
                "./src/test/java/co/decodable/sdk/pipeline/snippets/PurchaseOrderProcessingJob.java")
            .toURI()
            .toURL();

    Compilation compilation = compile(jobFile);

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH)
        .hasContents(
            CharSource.wrap(
                    "co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob.source-streams=purchase-orders\n"
                        + "co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob.sink-streams=purchase-orders-processed\n")
                .asByteSource(StandardCharsets.UTF_8));
  }

  @Test
  public void onlySourcesPresent() throws IOException {
    URL jobFile =
        new File("./src/test/java/co/decodable/sdk/pipeline/snippets/DummySourcesOnlyJob.java")
            .toURI()
            .toURL();

    Compilation compilation = compile(jobFile);

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH)
        .hasContents(
            CharSource.wrap(
                    "co.decodable.sdk.pipeline.snippets.DummySourcesOnlyJob.source-streams=source1,source2\n")
                .asByteSource(StandardCharsets.UTF_8));
  }

  @Test
  public void onlySinksPresent() throws IOException {
    URL jobFile =
        new File("./src/test/java/co/decodable/sdk/pipeline/snippets/DummySinksOnlyJob.java")
            .toURI()
            .toURL();

    Compilation compilation = compile(jobFile);

    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH)
        .hasContents(
            CharSource.wrap(
                    "co.decodable.sdk.pipeline.snippets.DummySinksOnlyJob.sink-streams=sink1,sink2\n")
                .asByteSource(StandardCharsets.UTF_8));
  }

  @Test
  public void shouldGenerateStreamNamesFileForMultipleFiles()
      throws MalformedURLException, Exception {
    URL jobFile1 =
        new File(
                "./src/test/java/co/decodable/sdk/pipeline/snippets/PurchaseOrderProcessingJob.java")
            .toURI()
            .toURL();
    URL jobFile2 =
        new File("./src/test/java/co/decodable/sdk/pipeline/snippets/DummySourcesOnlyJob.java")
            .toURI()
            .toURL();
    URL jobFile3 =
        new File("./src/test/java/co/decodable/sdk/pipeline/snippets/DummySinksOnlyJob.java")
            .toURI()
            .toURL();
    Compilation compilation = compile(jobFile1, jobFile2, jobFile3);

    assertThat(compilation).succeeded();
    var file = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH).get();
    var fileContents = file.getCharContent(false).toString();
    System.out.println(fileContents);
    assertThat(compilation)
        .generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH)
        .hasContents(
            CharSource.wrap(
                    "co.decodable.sdk.pipeline.snippets.DummySinksOnlyJob.sink-streams=sink1,sink2\n"
                        + "co.decodable.sdk.pipeline.snippets.DummySourcesOnlyJob.source-streams=source1,source2\n"
                        + "co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob.source-streams=purchase-orders\n"
                        + "co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob.sink-streams=purchase-orders-processed\n")
                .asByteSource(StandardCharsets.UTF_8));
  }

  @Test
  public void shouldWarnIfNoStreamsAreDeclared() throws MalformedURLException {
    var logger = Logger.getLogger(MetadataProcessor.class.getName());
    var handler = new TestHandler();
    logger.setUseParentHandlers(false);
    logger.addHandler(handler);
    var fileWithoutAnnotations =
        new File(
                "./src/test/java/co/decodable/sdk/pipeline/internal/config/metadata/MetadataProcessorTest.java")
            .toURI()
            .toURL();

    var compilation = compile(fileWithoutAnnotations);

    assertThat(compilation).succeeded();
    assertThat(handler.getRecords()).hasSize(1);
    var record = handler.getRecords().get(0);
    assertThat(record.getLevel()).isEqualTo(Level.WARNING);
    assertThat(record.getMessage()).contains("Neither source nor sink streams were declared");
  }

  private static Compilation compile(URL... files) {
    Arrays.stream(files).map(f -> JavaFileObjects.forResource(f)).collect(Collectors.toSet());
    return Compiler.javac()
        .withProcessors(new MetadataProcessor())
        .compile(
            Arrays.stream(files)
                .map(f -> JavaFileObjects.forResource(f))
                .collect(Collectors.toSet()));
  }

  private static class TestHandler extends Handler {

    private final List<LogRecord> records = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
      records.add(record);
    }

    @Override
    public void flush() {
      records.clear();
    }

    @Override
    public void close() throws SecurityException {}

    public List<LogRecord> getRecords() {
      return records;
    }
  }
}

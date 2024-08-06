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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.decodable.sdk.pipeline.internal.metadata.MetadataProcessor;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MetadataProcessorTest {

  private static final String OUTPUT_PATH = "META-INF/decodable/stream-names.properties";
  private static final String SOURCES_AND_SINKS_FILE_PATH =
      "./src/test/java/co/decodable/sdk/pipeline/snippets/PurchaseOrderProcessingJob.java";
  private static final String ONLY_SOURCES_FILE_PATH =
      "./src/test/java/co/decodable/sdk/pipeline/snippets/DummySourcesOnlyJob.java";
  private static final String ONLY_SINKS_FILE_PATH =
      "./src/test/java/co/decodable/sdk/pipeline/snippets/DummySinksOnlyJob.java";

  @Test
  public void shouldGenerateStreamNamesFile() throws IOException {
    URL jobFile1 = new File(SOURCES_AND_SINKS_FILE_PATH).toURI().toURL();
    URL jobFile2 = new File(ONLY_SOURCES_FILE_PATH).toURI().toURL();
    URL jobFile3 = new File(ONLY_SINKS_FILE_PATH).toURI().toURL();

    Compilation compilation = compile(jobFile1, jobFile2, jobFile3);

    assertThat(compilation).succeeded();
    var file = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH).get();
    var fileContents = file.getCharContent(false).toString();
    assertThat(fileContents)
        .startsWith("source-streams:\n")
        .contains("\nsink-streams:\n")
        .hasLineCount(6)
        .contains(
            "\nco.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob=purchase-orders\n")
        .contains(
            "\nco.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob=purchase-orders-processed\n")
        .containsPattern(
            Pattern.compile(
                "\\nco.decodable.sdk.pipeline.snippets.DummySourcesOnlyJob=source[1-2],source[1-2]\\n"))
        .containsPattern(
            Pattern.compile(
                "\\nco.decodable.sdk.pipeline.snippets.DummySinksOnlyJob=sink[1-2],sink[1-2]$"));
  }

  @Test
  public void onlySourcesPresent() throws IOException {
    URL jobFile = new File(ONLY_SOURCES_FILE_PATH).toURI().toURL();

    Compilation compilation = compile(jobFile);

    assertThat(compilation).succeeded();
    var file = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH).get();
    var fileContents = file.getCharContent(false).toString();
    assertThat(fileContents)
        .startsWith("source-streams:\n")
        .endsWith("\nsink-streams:\n")
        .hasLineCount(3)
        .containsPattern(
            Pattern.compile(
                "\\nco.decodable.sdk.pipeline.snippets.DummySourcesOnlyJob=source[1-2],source[1-2]\\n"));
  }

  @Test
  public void onlySinksPresent() throws IOException {
    URL jobFile = new File(ONLY_SINKS_FILE_PATH).toURI().toURL();

    Compilation compilation = compile(jobFile);

    assertThat(compilation).succeeded();
    var file = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, OUTPUT_PATH).get();
    var fileContents = file.getCharContent(false).toString();
    assertThat(fileContents)
        .startsWith("source-streams:\nsink-streams:\n")
        .hasLineCount(3)
        .containsPattern(
            Pattern.compile(
                "\\nco.decodable.sdk.pipeline.snippets.DummySinksOnlyJob=sink[1-2],sink[1-2]$"));
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

  @ParameterizedTest
  @ValueSource(
      strings = {
        "./src/test/java/co/decodable/sdk/pipeline/snippets/EnumWithSourceStreamsAnnotation.java",
        "./src/test/java/co/decodable/sdk/pipeline/snippets/InterfaceWithSourceStreamsAnnotation.java"
      })
  public void shouldThrowIfAnnotationIsUsedOnWrongType(String filePath)
      throws MalformedURLException {
    assertThatThrownBy(() -> compile(new File(filePath).toURI().toURL()))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "@SourceStreams and @SinkStreams annotations can only be used at class level");
  }

  private static Compilation compile(URL... fileURLs) {
    return Compiler.javac()
        .withProcessors(new MetadataProcessor())
        .compile(
            Arrays.stream(fileURLs).map(JavaFileObjects::forResource).collect(Collectors.toList()));
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

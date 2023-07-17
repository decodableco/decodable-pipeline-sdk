/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal.metadata;

import co.decodable.sdk.pipeline.metadata.SinkStreams;
import co.decodable.sdk.pipeline.metadata.SourceStreams;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * An annotation processor for generating the file {@code
 * "META-INF/decodable/stream-names.properties"}, allowing the Decodable platform to display the
 * streams connected to a custom pipeline.
 */
@SupportedAnnotationTypes({
  "co.decodable.sdk.pipeline.metadata.SourceStreams",
  "co.decodable.sdk.pipeline.metadata.SinkStreams"
})
public class MetadataProcessor extends AbstractProcessor {

  private static final String STREAM_NAMES_FILE = "META-INF/decodable/stream-names.properties";

  private final Set<String> allSourceStreams;
  private final Set<String> allSinkStreams;

  public MetadataProcessor() {
    allSourceStreams = new HashSet<>();
    allSinkStreams = new HashSet<>();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
      for (Element annotated : annotatedElements) {
        SourceStreams sourceStreams = annotated.getAnnotation(SourceStreams.class);
        allSourceStreams.addAll(Arrays.asList(sourceStreams.value()));

        SinkStreams sinkStreams = annotated.getAnnotation(SinkStreams.class);
        allSinkStreams.addAll(Arrays.asList(sinkStreams.value()));
      }
    }

    if (roundEnv.processingOver()) {
      try {
        FileObject streamNamesFile =
            processingEnv
                .getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", STREAM_NAMES_FILE);

        try (PrintWriter out = new PrintWriter(streamNamesFile.openWriter())) {
          out.println(
              "source-streams=" + allSourceStreams.stream().collect(Collectors.joining(",")));
          out.println("sink-streams=" + allSinkStreams.stream().collect(Collectors.joining(",")));
        }
      } catch (IOException e) {
        processingEnv
            .getMessager()
            .printMessage(
                Kind.ERROR, "Couldn't generate stream-names.properties file: " + e.getMessage());
      }
    }

    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }
}

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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * An annotation processor for generating the file {@code
 * "META-INF/decodable/stream-names.properties"}, allowing the Decodable platform to display the
 * streams connected to a custom pipeline. Note: We handle the supported annotation types inside the
 * processor in order to print a warning when no source or sink streams were declared.
 */
@SupportedAnnotationTypes({"*"})
public class MetadataProcessor extends AbstractProcessor {

  private static final Logger LOGGER = Logger.getLogger(MetadataProcessor.class.getName());
  private static final String STREAM_NAMES_FILE = "META-INF/decodable/stream-names.properties";
  private final Set<String> supportedAnnotationClassNames =
      Set.of(SourceStreams.class, SinkStreams.class).stream()
          .map(Class::getName)
          .collect(Collectors.toSet());

  private final Map<String, List<String>> sourceStreamsByEntryClass;
  private final Map<String, List<String>> sinkStreamsByEntryClass;

  public MetadataProcessor() {
    sourceStreamsByEntryClass = new HashMap<>();
    sinkStreamsByEntryClass = new HashMap<>();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    var filteredAnnotations =
        annotations.stream()
            .filter(
                annotation ->
                    supportedAnnotationClassNames.contains(
                        annotation.getQualifiedName().toString()))
            .collect(Collectors.toSet());
    for (TypeElement annotation : filteredAnnotations) {
      Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
      for (Element annotated : annotatedElements) {
        var elementKind = annotated.getKind();
        if (elementKind != ElementKind.CLASS) {
          throw new RuntimeException(
              "@SourceStreams and @SinkStreams annotations can only be used at class level");
        }
        var qualifiedClassName = ((TypeElement) annotated).getQualifiedName();
        SourceStreams sourceStreams = annotated.getAnnotation(SourceStreams.class);
        if (sourceStreams != null && sourceStreams.value() != null) {
          sourceStreamsByEntryClass.put(
              qualifiedClassName.toString(), Arrays.asList(sourceStreams.value()));
        }

        SinkStreams sinkStreams = annotated.getAnnotation(SinkStreams.class);
        if (sinkStreams != null && sinkStreams.value() != null) {
          sinkStreamsByEntryClass.put(
              qualifiedClassName.toString(), Arrays.asList(sinkStreams.value()));
        }
      }
    }

    if (roundEnv.processingOver()) {
      if (sourceStreamsByEntryClass.isEmpty() && sinkStreamsByEntryClass.isEmpty()) {
        LOGGER.log(
            Level.WARNING,
            "Neither source nor sink streams were declared. No streams will be available to this pipeline. If this "
                + "is unintentional, please use the @SourceStreams and @SinkStreams annotations to declare source "
                + "and/or sink streams.");
      }
      try {
        FileObject streamNamesFile =
            processingEnv
                .getFiler()
                .createResource(StandardLocation.CLASS_OUTPUT, "", STREAM_NAMES_FILE);

        try (PrintWriter out = new PrintWriter(streamNamesFile.openWriter())) {
          out.println("source-streams:");
          sourceStreamsByEntryClass.forEach(
              (entryClass, streams) ->
                  out.println(
                      entryClass + "=" + streams.stream().collect(Collectors.joining(","))));
          out.println("sink-streams:");
          sinkStreamsByEntryClass.forEach(
              (entryClass, streams) ->
                  out.println(
                      entryClass + "=" + streams.stream().collect(Collectors.joining(","))));
        }
      } catch (IOException e) {
        processingEnv
            .getMessager()
            .printMessage(
                Kind.ERROR, "Couldn't generate stream-names.properties file: " + e.getMessage());
      }
    }

    // Since we pass all annotation types to this processor, we return false to make sure other
    // processors will still process their target annotations
    return false;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }
}

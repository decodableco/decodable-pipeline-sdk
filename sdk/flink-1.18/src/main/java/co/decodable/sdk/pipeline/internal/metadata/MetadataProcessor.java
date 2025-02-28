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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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

  private final Set<StreamConfig> streamConfigs;

  public MetadataProcessor() {
    streamConfigs = new TreeSet<MetadataProcessor.StreamConfig>();
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

        // safe cast, the annotation is only allowed for types
        StreamConfig config =
            new StreamConfig(((TypeElement) annotated).getQualifiedName().toString());
        streamConfigs.add(config);

        SourceStreams sourceStreams = annotated.getAnnotation(SourceStreams.class);
        if (sourceStreams != null && sourceStreams.value() != null) {
          config.addSourceStreams(Arrays.asList(sourceStreams.value()));
        }

        SinkStreams sinkStreams = annotated.getAnnotation(SinkStreams.class);
        if (sinkStreams != null && sinkStreams.value() != null) {
          config.addSinkStreams(Arrays.asList(sinkStreams.value()));
        }
      }
    }

    if (roundEnv.processingOver()) {
      if (streamConfigs.isEmpty()) {
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
          for (StreamConfig config : streamConfigs) {
            if (!config.sourceStreams.isEmpty()) {
              out.println(
                  config.className
                      + ".source-streams="
                      + config.sourceStreams.stream().collect(Collectors.joining(",")));
            }
            if (!config.sinkStreams.isEmpty()) {
              out.println(
                  config.className
                      + ".sink-streams="
                      + config.sinkStreams.stream().collect(Collectors.joining(",")));
            }
          }
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

  private static class StreamConfig implements Comparable<StreamConfig> {
    final String className;
    final Set<String> sourceStreams;
    final Set<String> sinkStreams;

    public StreamConfig(String className) {
      this.className = className;
      sourceStreams = new LinkedHashSet<String>();
      sinkStreams = new LinkedHashSet<String>();
    }

    public void addSourceStreams(Collection<String> sourceStreams) {
      this.sourceStreams.addAll(sourceStreams);
    }

    public void addSinkStreams(Collection<String> sinkStreams) {
      this.sinkStreams.addAll(sinkStreams);
    }

    @Override
    public int hashCode() {
      return Objects.hash(className);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      StreamConfig other = (StreamConfig) obj;
      return Objects.equals(className, other.className);
    }

    @Override
    public int compareTo(StreamConfig o) {
      return className.compareTo(o.className);
    }
  }
}

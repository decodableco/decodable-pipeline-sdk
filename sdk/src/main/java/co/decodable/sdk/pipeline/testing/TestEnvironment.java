/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.testing;

import co.decodable.sdk.pipeline.EnvironmentAccess.Environment;
import co.decodable.sdk.pipeline.util.Incubating;
import co.decodable.sdk.pipeline.util.Unmodifiable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An {@link Environment} implementation for testing purposes, allowing to define one or more
 * Decodable streams which then can be accessed from the job under test.
 */
@Incubating
public class TestEnvironment implements Environment {

  /** A builder for creating new {@link TestEnvironment} instances. */
  public static class Builder {

    private String bootstrapServers;
    private final Map<String, StreamConfiguration> streams = new HashMap<>();

    /** Specifies the bootstrap server(s) to be used. */
    public Builder withBootstrapServers(String bootstrapServers) {
      Objects.requireNonNull(bootstrapServers, "Bootstrap servers must be specified");
      this.bootstrapServers = bootstrapServers;
      return this;
    }

    /**
     * Specifies the names of the stream(s) which should be available via this test environment. At
     * least one stream name must be given.
     */
    public Builder withStreams(String firstStream, String... furtherStreams) {
      Objects.requireNonNull(firstStream, "At least one stream name must be specified");
      streams.put(firstStream, new StreamConfiguration(firstStream));

      if (furtherStreams != null) {
        for (String stream : furtherStreams) {
          streams.put(stream, new StreamConfiguration(stream));
        }
      }

      return this;
    }

    /** Returns a new {@link TestEnvironment} for the given configuration. */
    public TestEnvironment build() {
      return new TestEnvironment(bootstrapServers, streams);
    }
  }

  private static final String STREAM_CONFIG_TEMPLATE =
      "{\n"
          + "    \"properties\": {\n"
          + "        \"value.format\": \"debezium-json\",\n"
          + "        \"key.format\": \"json\",\n"
          + "        \"topic\": \"%s\",\n"
          + "        \"scan.startup.mode\": \"earliest-offset\",\n"
          + "        \"key.fields\": \"\\\"order_id\\\"\",\n"
          + "        \"sink.transactional-id-prefix\": \"tx-account-00000000-PIPELINE-af78c091-1686579235527\",\n"
          + "        \"sink.delivery-guarantee\": \"exactly-once\",\n"
          + "        \"properties.bootstrap.servers\": \"%s\",\n"
          + "        \"properties.transaction.timeout.ms\": \"900000\",\n"
          + "        \"properties.isolation.level\": \"read_committed\",\n"
          + "        \"properties.compression.type\": \"zstd\",\n"
          + "        \"properties.enable.idempotence\": \"true\"\n"
          + "    },\n"
          + "    \"name\": \"%s\"\n"
          + "}";
  @Unmodifiable private final Map<String, StreamConfiguration> streams;
  private final String bootstrapServers;

  private TestEnvironment(String bootstrapServers, Map<String, StreamConfiguration> streams) {
    this.bootstrapServers = bootstrapServers;
    this.streams = Collections.unmodifiableMap(streams);
  }

  /** Returns a builder for creating a new {@link TestEnvironment}. */
  public static Builder builder() {
    return new Builder();
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getEnvironmentConfiguration() {
    return streams.entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                e -> "DECODABLE_STREAM_CONFIG_" + e.getValue().id(),
                e ->
                    String.format(
                        STREAM_CONFIG_TEMPLATE,
                        e.getValue().topic(),
                        bootstrapServers,
                        e.getValue().name())));
  }

  /** Returns the name of the Kafka topic backing the given stream. */
  public String topicFor(String streamName) {
    StreamConfiguration config = streams.get(streamName);

    if (config == null) {
      throw new IllegalArgumentException("Stream '" + streamName + "' has not been configured");
    }

    return config.topic();
  }

  /** Returns the Kafka bootstrap server(s) configured for this environment. */
  public String bootstrapServers() {
    return bootstrapServers;
  }

  private static class StreamConfiguration {

    private final String name;
    private final String id;
    private final String topic;

    public StreamConfiguration(String name) {
      this.name = name;
      this.id = getRandomId();
      this.topic = "stream-00000000-" + id;
    }

    private static String getRandomId() {
      int digits = 8;
      return String.format("%0" + digits + "x", new BigInteger(digits * 4, new SecureRandom()));
    }

    public String name() {
      return name;
    }

    public String id() {
      return id;
    }

    public String topic() {
      return topic;
    }
  }
}

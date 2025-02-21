/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal.config;

import co.decodable.sdk.pipeline.StartupMode;
import co.decodable.sdk.pipeline.util.Unmodifiable;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class StreamConfig {

  /** Used by Flink to prefix all pass-through options for the Kafka producer/consumer. */
  private static final String PROPERTIES_PREFIX = "properties.";

  private final String id;
  private final String name;
  private final String bootstrapServers;
  private final String topic;
  private final StartupMode startupMode;
  private final String transactionalIdPrefix;
  private final String deliveryGuarantee;

  @Unmodifiable private final Map<String, String> properties;

  public StreamConfig(String id, String name, Map<String, String> properties) {
    this.id = id;
    this.name = name;
    this.bootstrapServers =
        properties.get(PROPERTIES_PREFIX + ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
    this.topic = properties.get("topic");
    this.startupMode = StartupMode.fromString(properties.get("scan.startup.mode"));
    this.transactionalIdPrefix = properties.get("sink.transactional-id-prefix");
    this.deliveryGuarantee = properties.get("sink.delivery-guarantee");
    this.properties =
        properties.entrySet().stream()
            .filter(e -> e.getKey().startsWith("properties"))
            .collect(
                Collectors.toUnmodifiableMap(e -> e.getKey().substring(11), e -> e.getValue()));
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String bootstrapServers() {
    return bootstrapServers;
  }

  public String topic() {
    return topic;
  }

  public StartupMode startupMode() {
    return startupMode;
  }

  public String transactionalIdPrefix() {
    return transactionalIdPrefix;
  }

  public String deliveryGuarantee() {
    return deliveryGuarantee;
  }

  public Map<String, String> kafkaProperties() {
    return properties;
  }
}

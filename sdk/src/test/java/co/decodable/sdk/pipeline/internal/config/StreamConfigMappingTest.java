/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import co.decodable.sdk.pipeline.StartupMode;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class StreamConfigMappingTest {

  @Test
  public void shouldParseStreamConfig() {
    String config =
        "{\n"
            + "    \"properties\": {\n"
            + "        \"value.format\": \"debezium-json\",\n"
            + "        \"key.format\": \"json\",\n"
            + "        \"topic\": \"stream-00000000-078fc8b5\",\n"
            + "        \"scan.startup.mode\": \"latest-offset\",\n"
            + "        \"key.fields\": \"\\\"shipment_id\\\"\",\n"
            + "        \"sink.transactional-id-prefix\": \"tx-account-00000000-PIPELINE-af78c091-1686579235527\",\n"
            + "        \"sink.delivery-guarantee\": \"exactly-once\",\n"
            + "        \"properties.bootstrap.servers\": \"my-kafka:9092\",\n"
            + "        \"properties.transaction.timeout.ms\": \"900000\",\n"
            + "        \"properties.isolation.level\": \"read_committed\",\n"
            + "        \"properties.compression.type\": \"zstd\",\n"
            + "        \"properties.enable.idempotence\": \"true\"\n"
            + "    },\n"
            + "    \"name\": \"shipments\"\n"
            + "}";

    StreamConfigMapping streamConfigMapping =
        new StreamConfigMapping(Map.of("DECODABLE_STREAM_CONFIG_078fc8b5", config));
    StreamConfig streamConfig = streamConfigMapping.determineConfig("shipments");

    assertEquals("078fc8b5", streamConfig.id());
    assertEquals("shipments", streamConfig.name());
    assertEquals("my-kafka:9092", streamConfig.bootstrapServers());
    assertEquals("stream-00000000-078fc8b5", streamConfig.topic());
    assertEquals(StartupMode.LATEST_OFFSET, streamConfig.startupMode());
    assertEquals(
        "tx-account-00000000-PIPELINE-af78c091-1686579235527",
        streamConfig.transactionalIdPrefix());
    assertEquals("exactly-once", streamConfig.deliveryGuarantee());
    assertThat(streamConfig.kafkaProperties())
        .contains(
            entry("bootstrap.servers", "my-kafka:9092"),
            entry("transaction.timeout.ms", "900000"),
            entry("isolation.level", "read_committed"),
            entry("compression.type", "zstd"),
            entry("enable.idempotence", "true"));
  }
}

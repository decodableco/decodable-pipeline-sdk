/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import co.decodable.sdk.pipeline.testing.KeyedPipelineTestContext;
import co.decodable.sdk.pipeline.testing.KeyedStreamRecord;
import co.decodable.sdk.pipeline.testing.TestEnvironment;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers // @start region="testing-custom-pipeline"
public class ChangeStreamJobTest {

  private static final String PURCHASE_ORDERS = "purchase-orders";
  private static final String PURCHASE_ORDERS_PROCESSED = "purchase-orders-processed";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Container
  public RedpandaContainer broker =
      new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

  @Test
  public void shouldUpperCaseCustomerNameForInserts() throws Exception {
    TestEnvironment testEnvironment =
        TestEnvironment.builder()
            .withBootstrapServers(broker.getBootstrapServers())
            .withStreams(PURCHASE_ORDERS, PURCHASE_ORDERS_PROCESSED)
            .build();

    try (KeyedPipelineTestContext ctx = new KeyedPipelineTestContext(testEnvironment)) {
      String key1 = "{\n" + "  \"order_id\" : 19001\n" + "}";
      String value1 =
          "{\n"
              + "    \"before\": null,\n"
              + "    \"after\": {\n"
              + "      \"order_id\": 19001,\n"
              + "      \"order_date\": \"2023-06-09 10:18:38\",\n"
              + "      \"customer_name\": \"Yolanda Hagenes\",\n"
              + "      \"price\": 15.00,\n"
              + "      \"product_id\": 108,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"op\": \"c\",\n"
              + "    \"ts_ms\": 1750237849371\n"
              + "  }";

      String key2 = "{\n" + "  \"order_id\" : 19002\n" + "}";
      String value2 =
          "{\n"
              + "    \"before\": null,\n"
              + "    \"after\": {\n"
              + "      \"order_id\": 19002,\n"
              + "      \"order_date\": \"2023-06-09 11:25:33\",\n"
              + "      \"customer_name\": \"Erwin Mausepeter\",\n"
              + "      \"price\": 35.00,\n"
              + "      \"product_id\": 22,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"op\": \"c\",\n"
              + "    \"ts_ms\": 1750237849371\n"
              + "  }";

      // given
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key1, value1));
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key2, value2));

      // when
      ctx.runJobAsync(ChangeStreamJob::main);

      KeyedStreamRecord<String, String> result1 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder1Key = OBJECT_MAPPER.readTree(result1.key());
      JsonNode purchaseOrder1 = OBJECT_MAPPER.readTree(result1.value());

      KeyedStreamRecord<String, String> result2 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder2Key = OBJECT_MAPPER.readTree(result2.key());
      JsonNode purchaseOrder2 = OBJECT_MAPPER.readTree(result2.value());

      // then
      assertAll(
          // order 19001
          () -> assertThat(purchaseOrder1Key.get("order_id").asLong()).isEqualTo(19001),
          () -> assertTrue(purchaseOrder1.get("before").isNull()),
          () ->
              assertThat(purchaseOrder1.get("after").get("customer_name").asText())
                  .isEqualTo("YOLANDA HAGENES"),
          () -> assertThat(purchaseOrder1.get("after").get("price").asDouble()).isEqualTo(15.0),
          () -> assertThat(purchaseOrder1.get("after").get("product_id").asInt()).isEqualTo(108)
      );
      assertAll(
          // order 19002
          () -> assertThat(purchaseOrder2Key.get("order_id").asLong()).isEqualTo(19002),
          () -> assertTrue(purchaseOrder2.get("before").isNull()),
          () ->
              assertThat(purchaseOrder2.get("after").get("customer_name").asText())
                  .isEqualTo("ERWIN MAUSEPETER"),
          () -> assertThat(purchaseOrder2.get("after").get("price").asDouble()).isEqualTo(35.0),
          () -> assertThat(purchaseOrder2.get("after").get("product_id").asInt()).isEqualTo(22)
      );
    }
  }

  @Test
  public void shouldUpperCaseCustomerNameForUpdates() throws Exception {
    TestEnvironment testEnvironment =
        TestEnvironment.builder()
            .withBootstrapServers(broker.getBootstrapServers())
            .withStreams(PURCHASE_ORDERS, PURCHASE_ORDERS_PROCESSED)
            .build();

    try (KeyedPipelineTestContext ctx = new KeyedPipelineTestContext(testEnvironment)) {
      String key1 = "{\n" + "  \"order_id\" : 19001\n" + "}";
      String value1 =
          "{\n"
              + "    \"before\": {\n"
              + "      \"order_id\": 19001,\n"
              + "      \"order_date\": \"2023-06-09 10:18:38\",\n"
              + "      \"customer_name\": \"Yolanda Hagenes\",\n"
              + "      \"price\": 15.00,\n"
              + "      \"product_id\": 108,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"after\": {\n"
              + "      \"order_id\": 19001,\n"
              + "      \"order_date\": \"2023-06-09 10:18:38\",\n"
              + "      \"customer_name\": \"Yolanda Hagenes\",\n"
              + "      \"price\": 51.00,\n"
              + "      \"product_id\": 10,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"op\": \"u\",\n"
              + "    \"ts_ms\": 1750237849371\n"
              + "  }";

      String key2 = "{\n" + "  \"order_id\" : 19002\n" + "}";
      String value2 =
          "{\n"
              + "    \"before\": {\n"
              + "      \"order_id\": 19002,\n"
              + "      \"order_date\": \"2023-06-09 11:25:33\",\n"
              + "      \"customer_name\": \"Erwin Mausepeter\",\n"
              + "      \"price\": 35.00,\n"
              + "      \"product_id\": 22,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"after\": {\n"
              + "      \"order_id\": 19002,\n"
              + "      \"order_date\": \"2023-06-09 11:25:33\",\n"
              + "      \"customer_name\": \"Erwin Mausepeter\",\n"
              + "      \"price\": 53.00,\n"
              + "      \"product_id\": 220,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"op\": \"u\",\n"
              + "    \"ts_ms\": 1750237849371\n"
              + "  }";
      // given
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key1, value1));
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key2, value2));

      // when
      ctx.runJobAsync(ChangeStreamJob::main);

      KeyedStreamRecord<String, String> result1 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder1Key = OBJECT_MAPPER.readTree(result1.key());
      JsonNode purchaseOrder1 = OBJECT_MAPPER.readTree(result1.value());

      KeyedStreamRecord<String, String> result2 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder2Key = OBJECT_MAPPER.readTree(result2.key());
      JsonNode purchaseOrder2 = OBJECT_MAPPER.readTree(result2.value());

      // then
      assertAll(
          // order 19001
          () -> assertThat(purchaseOrder1Key.get("order_id").asLong()).isEqualTo(19001),
          () -> assertFalse(purchaseOrder1.get("before").isNull()),
          () ->
              assertThat(purchaseOrder1.get("before").get("customer_name").asText())
                  .isEqualTo("YOLANDA HAGENES"),
          () ->
              assertThat(purchaseOrder1.get("after").get("customer_name").asText())
                  .isEqualTo("YOLANDA HAGENES"),
          () -> assertThat(purchaseOrder1.get("after").get("price").asDouble()).isEqualTo(51.0),
          () -> assertThat(purchaseOrder1.get("after").get("product_id").asInt()).isEqualTo(10)
      );
      assertAll(
          // order 19002
          () -> assertThat(purchaseOrder2Key.get("order_id").asLong()).isEqualTo(19002),
          () -> assertFalse(purchaseOrder2.get("before").isNull()),
          () ->
              assertThat(purchaseOrder2.get("before").get("customer_name").asText())
                  .isEqualTo("ERWIN MAUSEPETER"),
          () ->
              assertThat(purchaseOrder2.get("after").get("customer_name").asText())
                  .isEqualTo("ERWIN MAUSEPETER"),
          () -> assertThat(purchaseOrder2.get("after").get("price").asDouble()).isEqualTo(53.0),
          () -> assertThat(purchaseOrder2.get("after").get("product_id").asInt()).isEqualTo(220)
      );
    }
  }

  @Test
  public void shouldUpperCaseCustomerNameForDeletes() throws Exception {
    TestEnvironment testEnvironment =
        TestEnvironment.builder()
            .withBootstrapServers(broker.getBootstrapServers())
            .withStreams(PURCHASE_ORDERS, PURCHASE_ORDERS_PROCESSED)
            .build();

    try (KeyedPipelineTestContext ctx = new KeyedPipelineTestContext(testEnvironment)) {
      String key1 = "{\n" + "  \"order_id\" : 19001\n" + "}";
      String value1 =
          "{\n"
              + "    \"before\": {\n"
              + "      \"order_id\": 19001,\n"
              + "      \"order_date\": \"2023-06-09 10:18:38\",\n"
              + "      \"customer_name\": \"Yolanda Hagenes\",\n"
              + "      \"price\": 51.00,\n"
              + "      \"product_id\": 10,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"after\": null,\n"
              + "    \"op\": \"d\",\n"
              + "    \"ts_ms\": 1750237849371\n"
              + "  }";
      String key2 = "{\n" + "  \"order_id\" : 19002\n" + "}";
      String value2 =
          "{\n"
              + "    \"before\": {\n"
              + "      \"order_id\": 19002,\n"
              + "      \"order_date\": \"2023-06-09 11:25:33\",\n"
              + "      \"customer_name\": \"Erwin Mausepeter\",\n"
              + "      \"price\": 53.00,\n"
              + "      \"product_id\": 220,\n"
              + "      \"order_status\": false\n"
              + "    },\n"
              + "    \"after\": null,\n"
              + "    \"op\": \"d\",\n"
              + "    \"ts_ms\": 1750237849371\n"
              + "  }";

      // given
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key1, value1));
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key2, value2));

      // when
      ctx.runJobAsync(ChangeStreamJob::main);

      KeyedStreamRecord<String, String> result1 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder1Key = OBJECT_MAPPER.readTree(result1.key());
      JsonNode purchaseOrder1 = OBJECT_MAPPER.readTree(result1.value());

      KeyedStreamRecord<String, String> result2 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder2Key = OBJECT_MAPPER.readTree(result2.key());
      JsonNode purchaseOrder2 = OBJECT_MAPPER.readTree(result2.value());

      // then
      assertAll(
          // order 19001
          () -> assertThat(purchaseOrder1Key.get("order_id").asLong()).isEqualTo(19001),
          () ->
              assertThat(purchaseOrder1.get("before").get("customer_name").asText())
                  .isEqualTo("YOLANDA HAGENES"),
          () -> assertThat(purchaseOrder1.get("before").get("price").asDouble()).isEqualTo(51.0),
          () -> assertThat(purchaseOrder1.get("before").get("product_id").asInt()).isEqualTo(10),
          () -> assertTrue(purchaseOrder1.get("after").isNull())
      );
      assertAll(
          // order 19002
          () -> assertThat(purchaseOrder2Key.get("order_id").asLong()).isEqualTo(19002),
          () ->
              assertThat(purchaseOrder2.get("before").get("customer_name").asText())
                  .isEqualTo("ERWIN MAUSEPETER"),
          () -> assertThat(purchaseOrder2.get("before").get("price").asDouble()).isEqualTo(53.0),
          () -> assertThat(purchaseOrder2.get("before").get("product_id").asInt()).isEqualTo(220),
          () -> assertTrue(purchaseOrder2.get("after").isNull())
      );
    }
  }
}
// @end region="testing-custom-pipeline"

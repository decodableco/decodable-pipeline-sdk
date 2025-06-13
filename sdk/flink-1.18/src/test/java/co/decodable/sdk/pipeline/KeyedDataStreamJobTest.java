/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import co.decodable.sdk.pipeline.snippets.KeyedPurchaseOrderProcessingJob;
import co.decodable.sdk.pipeline.testing.*;
import java.util.concurrent.TimeUnit;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

@Testcontainers // @start region="testing-custom-pipeline"
public class KeyedDataStreamJobTest {

  private static final String PURCHASE_ORDERS = "purchase-orders";
  private static final String PURCHASE_ORDERS_PROCESSED = "purchase-orders-processed";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Container
  public RedpandaContainer broker =
      new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

  @Test
  public void shouldUpperCaseCustomerName() throws Exception {
    TestEnvironment testEnvironment =
        TestEnvironment.builder()
            .withBootstrapServers(broker.getBootstrapServers())
            .withStreams(PURCHASE_ORDERS, PURCHASE_ORDERS_PROCESSED)
            .build();

    try (KeyedPipelineTestContext ctx = new KeyedPipelineTestContext(testEnvironment)) {
      String key1 = "{\n" + "  \"order_id\" : 19001\n" + "}";
      String value1 =
          "{\n"
              + "  \"order_id\" : 19001,\n"
              + "  \"order_date\" : \"2023-06-09 10:18:38\",\n"
              + "  \"customer_name\" : \"Yolanda Hagenes\",\n"
              + "  \"price\" : 15.00,\n"
              + "  \"product_id\" : 108,\n"
              + "  \"order_status\" : false\n"
              + "}";

      String key2 = "{\n" + "  \"order_id\" : 19002\n" + "}";
      String value2 =
          "{\n"
              + "  \"order_id\" : 19002,\n"
              + "  \"order_date\" : \"2023-06-09 11:25:33\",\n"
              + "  \"customer_name\" : \"Erwin Mausepeter\",\n"
              + "  \"price\" : 35.00,\n"
              + "  \"product_id\" : 22,\n"
              + "  \"order_status\" : false\n"
              + "}";

      // given
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key1, value1));
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key2, value2));

      ctx.runJobAsync(KeyedPurchaseOrderProcessingJob::main);
      KeyedStreamRecord<String, String> result1 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      KeyedStreamRecord<String, String> result2 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder1 = OBJECT_MAPPER.readTree(result1.value());
      JsonNode purchaseOrder2 = OBJECT_MAPPER.readTree(result2.value());

      // then
      assertThat(purchaseOrder1.get("customer_name").asText()).isEqualTo("YOLANDA HAGENES");
      assertThat(purchaseOrder2.get("customer_name").asText()).isEqualTo("ERWIN MAUSEPETER");
    }
  }
}
// @end region="testing-custom-pipeline"

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import co.decodable.sdk.pipeline.testing.*;
import co.decodable.sdk.pipeline.testing.KeyedPipelineTestContext.ThrowingConsumer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class KeyedAppendStreamJobTests {

  static final String PURCHASE_ORDERS = "purchase-orders";
  static final String PURCHASE_ORDERS_PROCESSED = "purchase-orders-processed";

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Container
  public RedpandaContainer broker =
      new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

  @DisplayName("Test Example Jobs for DataStream and Table API")
  @ParameterizedTest(name = "[{index}] running test for {0}")
  @MethodSource("provideJobEntryPoints")
  public void shouldUpperCaseCustomerName(String jobName, ThrowingConsumer<String[]> mainMethod) throws Exception {
    TestEnvironment testEnvironment =
        TestEnvironment.builder()
            .withBootstrapServers(broker.getBootstrapServers())
            .withStreams(PURCHASE_ORDERS, PURCHASE_ORDERS_PROCESSED)
            .build();

    try (KeyedPipelineTestContext ctx = new KeyedPipelineTestContext(testEnvironment)) {
      String key1 =
              "{\n"
                + "  \"order_id\" : 19001\n"
              + "}";
      String value1 =
              "{\n"
              + "  \"order_id\" : 19001,\n"
              + "  \"order_date\" : \"2023-06-09 10:18:38\",\n"
              + "  \"customer_name\" : \"Yolanda Hagenes\",\n"
              + "  \"price\" : 15.00,\n"
              + "  \"product_id\" : 108,\n"
              + "  \"order_status\" : false\n"
              + "}";

      String key2 =
              "{\n"
                + "  \"order_id\" : 19002\n"
              + "}";
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
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key1,value1));
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(key2,value2));

      ctx.runJobAsync(mainMethod);
      KeyedStreamRecord<String,String> result1 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      KeyedStreamRecord<String,String> result2 =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder1 = OBJECT_MAPPER.readTree(result1.value());
      JsonNode purchaseOrder2 = OBJECT_MAPPER.readTree(result2.value());

      // then
      assertThat(purchaseOrder1.get("customer_name").asText()).isEqualTo("YOLANDA HAGENES");
      assertThat(purchaseOrder2.get("customer_name").asText()).isEqualTo("ERWIN MAUSEPETER");
    }
  }

  static Stream<Arguments> provideJobEntryPoints() {
    return Stream.of(
      Arguments.of(KeyedAppendStreamJob.class.getSimpleName(),(ThrowingConsumer<String[]>) KeyedAppendStreamJob::main),
      Arguments.of(KeyedAppendStreamJobTableAPI.class.getSimpleName(),(ThrowingConsumer<String[]>) KeyedAppendStreamJobTableAPI::main)
    );
  }

}

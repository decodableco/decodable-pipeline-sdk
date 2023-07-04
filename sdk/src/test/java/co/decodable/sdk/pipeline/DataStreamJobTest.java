/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob;
import co.decodable.sdk.pipeline.testing.PipelineTestContext;
import co.decodable.sdk.pipeline.testing.StreamRecord;
import co.decodable.sdk.pipeline.testing.TestEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

@Testcontainers // @start region="testing-custom-pipeline"
public class DataStreamJobTest {

  private static final String PURCHASE_ORDERS = "purchase-orders";
  private static final String PURCHASE_ORDERS_PROCESSED = "purchase-orders-processed";

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

    try (PipelineTestContext ctx = new PipelineTestContext(testEnvironment)) {
      String value =
          "{\n"
              + "  \"order_id\" : 19001,\n"
              + "  \"order_date\" : \"2023-06-09 10:18:38\",\n"
              + "  \"customer_name\" : \"Yolanda Hagenes\",\n"
              + "  \"price\" : 15.00,\n"
              + "  \"product_id\" : 108,\n"
              + "  \"order_status\" : false\n"
              + "}";

      // given
      ctx.stream(PURCHASE_ORDERS).add(new StreamRecord<>(value));

      // when (as an example, PurchaseOrderProcessingJob upper-cases the customer name)
      ctx.runJobAsync(PurchaseOrderProcessingJob::main);

      StreamRecord<String> result =
          ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      ObjectNode purchaseOrder = (ObjectNode) new ObjectMapper().readTree(result.value());

      // then
      assertThat(purchaseOrder.get("customer_name").asText()).isEqualTo("YOLANDA HAGENES");
    }
  }
}
// @end region="testing-custom-pipeline"

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import co.decodable.sdk.pipeline.snippets.PurchasingOrderProcessingTableAPIJob;
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

// spotless:off
@Testcontainers // @start region="testing-custom-pipeline"
public class TableAPIJobTest {

    static final String PURCHASE_ORDERS_STREAM = "purchase-orders";
    static final String PURCHASE_ORDERS_PROCESSED_STREAM = "purchase-orders-processed";

    @Container
    public RedpandaContainer broker =
            new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

    @Test
    public void runTest() throws Exception {
        TestEnvironment testEnvironment =
                TestEnvironment.builder()
                        .withBootstrapServers(broker.getBootstrapServers())
                        .withStreams(PURCHASE_ORDERS_STREAM, PURCHASE_ORDERS_PROCESSED_STREAM)
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
            ctx.stream(PURCHASE_ORDERS_STREAM).add(new StreamRecord<>(value));
            ctx.stream(PURCHASE_ORDERS_STREAM).add(new StreamRecord<>(value2));

            ctx.runJobAsync(PurchasingOrderProcessingTableAPIJob::main);

            StreamRecord<String> result =
                    ctx.stream(PURCHASE_ORDERS_PROCESSED_STREAM).takeOne().get(30, TimeUnit.SECONDS);
            StreamRecord<String> result2 =
                    ctx.stream(PURCHASE_ORDERS_PROCESSED_STREAM).takeOne().get(30, TimeUnit.SECONDS);
            ObjectNode purchaseOrder = (ObjectNode) new ObjectMapper().readTree(result.value());
            ObjectNode purchaseOrder2 = (ObjectNode) new ObjectMapper().readTree(result2.value());

            // then
            assertThat(purchaseOrder.get("customer_name").asText()).isEqualTo("YOLANDA HAGENES");
            assertThat(purchaseOrder2.get("customer_name").asText()).isEqualTo("ERWIN MAUSEPETER");
        }


    } // @end region="custom-pipeline"
}
//spotless:on

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.decodable.sdk.EnvironmentAccess;
import co.decodable.sdk.testing.TestEnvironment;

@Testcontainers
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

	    EnvironmentAccess.setEnvironment(testEnvironment);

    // 1. insert a record into the source stream
    try (var producer = new KafkaProducer<String, String>(producerProperties())) {
      String key = "19001";
      String value = "{\n"
          + "  \"order_id\" : 19001,\n"
          + "  \"order_date\" : \"2023-06-09 10:18:38\",\n"
          + "  \"customer_name\" : \"Muriel Auer\",\n"
          + "  \"price\" : 15.00,\n"
          + "  \"product_id\" : 108,\n"
          + "  \"order_status\" : false\n"
          + "}";

      Future<RecordMetadata> sent =
          producer.send(new ProducerRecord<String, String>(testEnvironment.topicFor(PURCHASE_ORDERS), key, value));

      // wait for record to be ack-ed
      sent.get();
    }


    // 2. Run the stream processing job
    CompletableFuture<Void> handle =
        CompletableFuture.runAsync(
            () -> {
              try {
                DataStreamJob.main(new String[0]);
              } catch (Exception e) {
                throw new RuntimeException("Job failed", e);
              }
            });

    // 3. assert the processed record on the output stream
    try (var consumer = new KafkaConsumer<String, String>(consumerProperties())) {
      consumer.subscribe(List.of(testEnvironment.topicFor(PURCHASE_ORDERS_PROCESSED)));

      Awaitility.await()
          .atMost(Duration.ofSeconds(10L))
          .until(
              () -> {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                  ObjectNode purchaseOrder = (ObjectNode) new ObjectMapper().readTree(record.value());
                  if (purchaseOrder.get("customer_name").asText().equals("MURIEL AUER")) {
                    return true;
                  }
                }

                return false;
              });
    } catch (ConditionTimeoutException e) {
      fail("Expected record not received in time");
    } finally {
      try {
        handle.get(0, TimeUnit.SECONDS);
      } catch (TimeoutException | ExecutionException | InterruptedException e) {
        handle.cancel(true);
      }
    }
  }

  private Properties producerProperties() {
    var props = new Properties();
    props.put("bootstrap.servers", broker.getBootstrapServers());
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    return props;
  }

  private Properties consumerProperties() {
    var consumerProps = new Properties();
    consumerProps.put("bootstrap.servers", broker.getBootstrapServers());
    consumerProps.put(
        "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProps.put(
        "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProps.put("auto.offset.reset", "earliest");
    consumerProps.put("group.id", "my-group");
    return consumerProps;
  }
}

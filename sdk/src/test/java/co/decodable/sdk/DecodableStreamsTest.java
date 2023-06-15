/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk;

import static org.junit.jupiter.api.Assertions.fail;

import co.decodable.sdk.testing.TestEnvironment;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
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

@Testcontainers
public class DecodableStreamsTest {

  private static final String PURCHASE_ORDERS = "purchase-orders";
  private static final String PURCHASE_ORDERS_PROCESSED = "purchase-orders-processed";

  @Container
  public RedpandaContainer broker =
      new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

  @Test
  public void shouldProcessExistingRecordInKafkaTopic() throws Exception {
    TestEnvironment testEnvironment =
        TestEnvironment.builder()
            .withBootstrapServers(broker.getBootstrapServers())
            .withStreams(PURCHASE_ORDERS, PURCHASE_ORDERS_PROCESSED)
            .build();

    EnvironmentAccess.setEnvironment(testEnvironment);

    // 1. insert a record into the source stream
    try (var producer = new KafkaProducer<String, String>(producerProperties())) {
      String key = "my-key";
      String value = "my-value";

      Future<RecordMetadata> sent =
          producer.send(
              new ProducerRecord<>(
                  testEnvironment.topicFor(PURCHASE_ORDERS), key, value));

      // wait for record to be ack-ed
      sent.get();
    }

    // 2. set up a Flink job for processing that stream
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DecodableStreamSource<String> source =
        DecodableStreamSource.builder().withStreamName(PURCHASE_ORDERS).build();

    DecodableStreamSink<String> sink =
        DecodableStreamSink.builder().withStreamName(PURCHASE_ORDERS_PROCESSED).build();

    DataStream<String> stream =
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "Purchase Orders Source")
            .map(String::toUpperCase);

    stream.sinkTo(sink);

    CompletableFuture<Void> handle =
        CompletableFuture.runAsync(
            () -> {
              try {
                env.execute();
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
                  if (record.value().equals("MY-VALUE")) {
                    return true;
                  }
                }

                return false;
              });
    } catch (ConditionTimeoutException e) {
      fail("Expected message not received in time");
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

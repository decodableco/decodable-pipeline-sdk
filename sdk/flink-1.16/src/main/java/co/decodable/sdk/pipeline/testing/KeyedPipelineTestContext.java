/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.testing;

import co.decodable.sdk.pipeline.EnvironmentAccess;
import co.decodable.sdk.pipeline.util.Incubating;
import java.lang.System.Logger.Level;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Provides access to keyed Decodable streams during testing as well as the ability to run custom
 * Flink jobs.
 */
@Incubating
public class KeyedPipelineTestContext implements AutoCloseable {

  private static final System.Logger LOGGER =
      System.getLogger(KeyedPipelineTestContext.class.getName());

  private final TestEnvironment testEnvironment;
  private final KafkaProducer<String, String> producer;
  private final Map<String, KeyedDecodableStreamImpl> streams;
  private final ExecutorService executorService;

  /** Creates a new testing context, using the given {@link TestEnvironment}. */
  public KeyedPipelineTestContext(TestEnvironment testEnvironment) {
    EnvironmentAccess.setEnvironment(testEnvironment);
    this.testEnvironment = testEnvironment;
    this.producer =
        new KafkaProducer<String, String>(producerProperties(testEnvironment.bootstrapServers()));
    this.streams = new HashMap<>();
    this.executorService = Executors.newCachedThreadPool();
  }

  private static Properties producerProperties(String bootstrapServers) {
    var props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    return props;
  }

  private static Properties consumerProperties(String bootstrapServers) {
    var consumerProps = new Properties();
    consumerProps.put("bootstrap.servers", bootstrapServers);
    consumerProps.put(
        "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProps.put(
        "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerProps.put("auto.offset.reset", "earliest");
    consumerProps.put("group.id", "my-group");
    return consumerProps;
  }

  /** Returns a stream for the given name. */
  public KeyedDecodableStream<String, String> stream(String name) {
    KafkaConsumer<String, String> consumer =
        new KafkaConsumer<String, String>(consumerProperties(testEnvironment.bootstrapServers()));
    consumer.subscribe(Collections.singleton(testEnvironment.topicFor(name)));

    return streams.computeIfAbsent(name, n -> new KeyedDecodableStreamImpl(n, consumer));
  }

  /** Asynchronously executes the given Flink job main method. */
  public void runJobAsync(ThrowingConsumer<String[]> jobMainMethod, String... args)
      throws Exception {
    executorService.submit(
        () -> {
          try {
            jobMainMethod.accept(args);
          } catch (InterruptedException e) {
            LOGGER.log(Level.INFO, "Job aborted");
          } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Job failed", e);
          }
        });
  }

  @Override
  public void close() throws Exception {
    try {
      producer.close();

      executorService.shutdownNow();
      executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
      for (KeyedDecodableStreamImpl stream : streams.values()) {
        stream.consumer.close();
      }
    } catch (Exception e) {
      throw new RuntimeException("Couldn't close testing context", e);
    } finally {
      EnvironmentAccess.resetEnvironment();
    }
  }

  /**
   * A {@link Consumer} variant which allows for declared checked exception types.
   *
   * @param <T> The consumed data type.
   */
  @FunctionalInterface
  public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
  }

  private class KeyedDecodableStreamImpl implements KeyedDecodableStream<String, String> {

    private final String streamName;
    private final KafkaConsumer<String, String> consumer;
    private final List<ConsumerRecord<String, String>> consumed;

    public KeyedDecodableStreamImpl(String streamName, KafkaConsumer<String, String> consumer) {
      this.streamName = streamName;
      this.consumer = consumer;
      this.consumed = new ArrayList<>();
    }

    @Override
    public void add(KeyedStreamRecord<String, String> streamRecord) {
      Future<RecordMetadata> sent =
          producer.send(
              new ProducerRecord<>(
                  testEnvironment.topicFor(streamName), streamRecord.key(), streamRecord.value()));

      // wait for record to be ack-ed
      try {
        sent.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException("Couldn't send record", e);
      }
    }

    @Override
    public Future<KeyedStreamRecord<String, String>> takeOne() {
      return ((CompletableFuture<List<KeyedStreamRecord<String, String>>>) take(1))
          .thenApply(l -> l.get(0));
    }

    @Override
    public Future<List<KeyedStreamRecord<String, String>>> take(int n) {
      return CompletableFuture.supplyAsync(
          () -> {
            while (consumed.size() < n) {
              ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(20));
              for (ConsumerRecord<String, String> record : records) {
                consumed.add(record);
              }
            }

            List<KeyedStreamRecord<String, String>> result =
                consumed.subList(0, n).stream()
                    .map(cr -> new KeyedStreamRecord<>(cr.key(), cr.value()))
                    .collect(Collectors.toList());

            consumed.subList(0, n).clear();

            return result;
          },
          executorService);
    }
  }
}

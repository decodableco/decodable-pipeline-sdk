package co.decodable.examples.cpdemo;

import co.decodable.sdk.pipeline.testing.KeyedPipelineTestContext;
import co.decodable.sdk.pipeline.testing.KeyedStreamRecord;
import co.decodable.sdk.pipeline.testing.TestEnvironment;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Testcontainers
public class MixedAppendStreamJobTests {

  static final String PURCHASE_ORDERS = "purchase-orders";
  static final String PURCHASE_ORDERS_PROCESSED = "purchase-orders-processed";

  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Container
  public RedpandaContainer broker =
          new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

  @DisplayName("Test Example Job for Keyed-to-Keyless Append Streams")
  @Test
  public void shouldUpperCaseCustomerNameWhenRunningKeyed2KeylessAppendStreamJob() throws Exception {
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

      // when
      ctx.runJobAsync(Keyed2KeylessAppendStreamJob::main);

      KeyedStreamRecord<String,String> result1 =
              ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      var purchaseOrder1Key = result1.key();
      JsonNode purchaseOrder1 = OBJECT_MAPPER.readTree(result1.value());

      KeyedStreamRecord<String,String> result2 =
              ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      var purchaseOrder2Key = result2.key();
      JsonNode purchaseOrder2 = OBJECT_MAPPER.readTree(result2.value());

      // then
      assertAll(
              () -> assertThat(purchaseOrder1Key).isNull(),
              () -> assertThat(purchaseOrder1.get("customer_name").asText()).isEqualTo("YOLANDA HAGENES")
      );
      assertAll(
              () -> assertThat(purchaseOrder2Key).isNull(),
              () -> assertThat(purchaseOrder2.get("customer_name").asText()).isEqualTo("ERWIN MAUSEPETER")
      );
    }
  }

  @DisplayName("Test Example Job for Keyless-to-Keyed Append Streams")
  @Test
  public void shouldUpperCaseCustomerNameWhenRunningKeyless2KeyedAppendStreamJob() throws Exception {
    TestEnvironment testEnvironment =
            TestEnvironment.builder()
                    .withBootstrapServers(broker.getBootstrapServers())
                    .withStreams(PURCHASE_ORDERS, PURCHASE_ORDERS_PROCESSED)
                    .build();

    try (KeyedPipelineTestContext ctx = new KeyedPipelineTestContext(testEnvironment)) {
      String value1 =
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
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(null,value1));
      ctx.stream(PURCHASE_ORDERS).add(new KeyedStreamRecord<>(null,value2));

      // when
      ctx.runJobAsync(Keyless2KeyedAppendStreamJob::main);

      KeyedStreamRecord<String,String> result1 =
              ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder1Key = OBJECT_MAPPER.readTree(result1.key());
      JsonNode purchaseOrder1 = OBJECT_MAPPER.readTree(result1.value());

      KeyedStreamRecord<String,String> result2 =
              ctx.stream(PURCHASE_ORDERS_PROCESSED).takeOne().get(30, TimeUnit.SECONDS);
      JsonNode purchaseOrder2Key = OBJECT_MAPPER.readTree(result2.key());
      JsonNode purchaseOrder2 = OBJECT_MAPPER.readTree(result2.value());

      // then
      assertAll(
              () -> assertThat(purchaseOrder1Key.get("order_id").asLong()).isEqualTo(19001),
              () -> assertThat(purchaseOrder1.get("customer_name").asText()).isEqualTo("YOLANDA HAGENES")
      );
      assertAll(
              () -> assertThat(purchaseOrder2Key.get("order_id").asLong()).isEqualTo(19002),
              () -> assertThat(purchaseOrder2.get("customer_name").asText()).isEqualTo("ERWIN MAUSEPETER")
      );
    }
  }

}

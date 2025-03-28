package co.decodable.examples.cpdemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.connector.datagen.functions.FromElementsGeneratorFunction;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JobTestsWithoutKafka {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void dataStreamJobTest() throws Exception {
        // given
        String value1 = "{\n"
                + "  \"order_id\" : 19001,\n"
                + "  \"order_date\" : \"2023-06-09 10:18:38\",\n"
                + "  \"customer_name\" : \"Yolanda Hagenes\",\n"
                + "  \"price\" : 15.00,\n"
                + "  \"product_id\" : 108,\n"
                + "  \"order_status\" : false\n"
                + "}";
        String value2 = "{\n"
                + "  \"order_id\" : 19002,\n"
                + "  \"order_date\" : \"2023-06-09 11:25:33\",\n"
                + "  \"customer_name\" : \"Erwin Mausepeter\",\n"
                + "  \"price\" : 35.00,\n"
                + "  \"product_id\" : 22,\n"
                + "  \"order_status\" : false\n"
                + "}";

        // when
        var testSource = new TestCollectionSource<String>(String.class, List.of(value1, value2),
                TypeInformation.of(String.class));
        var testSink = new TestCollectionSink<String>();

        var job = new DataStreamJob(testSource, testSink);
        job.run();

        // then
        var results = testSink.getElements();
        assertEquals("wrong number of elements in the sink", 2, results.size());

        var names = testSink.getElements().stream().map(json ->  {
            try {
                return ((ObjectNode) OBJECT_MAPPER.readTree(json)).get("customer_name").asText();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } 
        }).collect(Collectors.toList());
        
        assertEquals("wrong number of elements in the sink", 2, names.size());
        assertTrue(names.containsAll(List.of("YOLANDA HAGENES","ERWIN MAUSEPETER")));
    }

    @Test
    public void tableApiJobTest() throws Exception {
        // given
        PurchaseOrder po1 = new PurchaseOrder();
        po1.orderId = 19001;
        po1.orderDate = "2023-06-09 10:18:38";
        po1.customerName = "Yolanda Hagenes";
        po1.price = 15.00;
        po1.productId = 108;
        po1.orderStatus = false;

        PurchaseOrder po2 = new PurchaseOrder();
        po2.orderId = 19002;
        po2.orderDate = "2023-06-09 11:25:33";
        po2.customerName = "Erwin Mausepeter";
        po2.price = 35.00;
        po2.productId = 22;
        po2.orderStatus = false;

        // when
        var testSource = new TestCollectionSource<PurchaseOrder>(PurchaseOrder.class, List.of(po1, po2),
                TypeInformation.of(PurchaseOrder.class));
        var testSink = new TestCollectionSink<PurchaseOrder>();

        var job = new TableAPIJob(testSource, testSink);
        job.run();

        // then
        var names = testSink.getElements().stream().map(po -> po.customerName).collect(Collectors.toList());
        assertEquals("wrong number of elements in the sink", 2, names.size());
        assertTrue(names.containsAll(List.of("YOLANDA HAGENES","ERWIN MAUSEPETER")));
    }

    @AfterEach
    void clearTestSink() {
        TestCollectionSink.clearElements();
    }

    public static class TestCollectionSource<OUT> extends DataGeneratorSource<OUT> {

        @SuppressWarnings("unchecked")
        public TestCollectionSource(Class<OUT> type, Collection<OUT> data, TypeInformation<OUT> typeInfo) {
            super(new FromElementsGeneratorFunction<>(
                typeInfo,data.toArray((OUT[]) Array.newInstance(type, data.size()))), data.size(), typeInfo);
        }

    }

    public static class TestCollectionSink<IN> implements Sink<IN> {

        private static final List<Object> ELEMENTS = Collections.synchronizedList(new ArrayList<>());

        @Override
        public SinkWriter<IN> createWriter(InitContext context) throws IOException {
            return new SinkWriter<IN>() {
                @Override
                public void write(IN element, Context context)
                        throws IOException, InterruptedException {
                    ELEMENTS.add(element);
                }

                @Override
                public void flush(boolean endOfInput) throws IOException, InterruptedException {
                    // no-op
                }

                @Override
                public void close() throws Exception {
                    // no-op
                }
            };
        }

        @SuppressWarnings("unchecked")
        public List<IN> getElements() {
            return Collections.unmodifiableList(
                    ELEMENTS.stream().map(e -> (IN) e).collect(Collectors.toList()));
        }

        public static void clearElements() {
            ELEMENTS.clear();
        }

    }

}

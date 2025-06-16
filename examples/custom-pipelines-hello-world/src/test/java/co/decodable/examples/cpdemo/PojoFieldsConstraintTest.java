package co.decodable.examples.cpdemo;

import co.decodable.examples.cpdemo.model.OrderKey;
import co.decodable.examples.cpdemo.model.PurchaseOrder;
import co.decodable.sdk.pipeline.serde.SerializationConstraintsValidator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PojoFieldsConstraintTest {

  @ParameterizedTest
  @MethodSource("provideKeyAndValueClasses")
  void checkAllKeyFieldsPresentInValueFields(Class<?> keyClass, Class<?> valueClass) {
    assertDoesNotThrow(() -> SerializationConstraintsValidator.checkAllKeyFieldsPresentInValue(keyClass, valueClass));
  }

  static Stream<Arguments> provideKeyAndValueClasses() {
    return Stream.of(
            Arguments.of(OrderKey.class, PurchaseOrder.class)
            //TODO: add your key-value POJO types to check here...
            //Arguments.of(MyPojoKey.class, MyPojoValue.class)
    );
  }

}

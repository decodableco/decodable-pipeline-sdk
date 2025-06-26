/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import co.decodable.sdk.pipeline.model.OrderKey;
import co.decodable.sdk.pipeline.model.PurchaseOrder;
import co.decodable.sdk.pipeline.serde.SerializationConstraintsValidator;
import java.util.stream.Stream;
import org.apache.flink.types.PojoTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PojoTypeTest {

  @ParameterizedTest
  @MethodSource("provideKeyAndValueClassesForCustomSerializerChecks")
  void allKeyFieldsPresentInValueFieldsTest(Class<?> keyClass, Class<?> valueClass) {
    assertDoesNotThrow(
        () ->
            SerializationConstraintsValidator.checkAllKeyFieldsPresentInValue(
                keyClass, valueClass));
  }

  @ParameterizedTest
  @MethodSource("providePojoClassesForFlinkSerdeChecks")
  void isSerializableAsPojoTest(Class<?> pojoClass) {
    assertDoesNotThrow(() -> PojoTestUtils.assertSerializedAsPojo(pojoClass));
  }

  @ParameterizedTest
  @MethodSource("providePojoClassesForFlinkSerdeChecks")
  void isSerializableAsPojoWithoutKryoTest(Class<?> pojoClass) {
    assertDoesNotThrow(() -> PojoTestUtils.assertSerializedAsPojoWithoutKryo(pojoClass));
  }

  static Stream<Arguments> provideKeyAndValueClassesForCustomSerializerChecks() {
    return Stream.of(Arguments.of(OrderKey.class, PurchaseOrder.class));
  }

  static Stream<Arguments> providePojoClassesForFlinkSerdeChecks() {
    return Stream.of(
        Arguments.of(OrderKey.class),
        Arguments.of(PurchaseOrder.class),
        Arguments.of(co.decodable.sdk.pipeline.model.append.KeylessPurchaseOrder.class),
        Arguments.of(co.decodable.sdk.pipeline.model.append.KeyedPurchaseOrder.class),
        Arguments.of(co.decodable.sdk.pipeline.model.change.KeyedPurchaseOrder.class),
        Arguments.of(co.decodable.sdk.pipeline.model.change.PurchaseOrderEnvelope.class));
  }
}

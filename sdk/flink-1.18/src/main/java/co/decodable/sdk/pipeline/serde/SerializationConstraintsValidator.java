/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.serde;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationConstraintsValidator {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void checkAllKeyFieldsPresentInValue(Class<?> keyType, Class<?> valueType) {
    Objects.requireNonNull(keyType, "keyType must not be null");
    Objects.requireNonNull(valueType, "valueType must not be null");
    // NOTE: For "keyless" processing the default serializer might be used with keyType Void
    // which means such a check doesn't need to / couldn't be performed anyway...
    if (Void.class.equals(keyType)) {
      return;
    }
    try {
      var dummyKey = keyType.getDeclaredConstructor().newInstance();
      var keyFieldsMap =
          OBJECT_MAPPER.readValue(
              OBJECT_MAPPER.writeValueAsString(dummyKey), new TypeReference<Map<String, ?>>() {});
      var dummyValue = valueType.getDeclaredConstructor().newInstance();
      var valueFieldsMap =
          OBJECT_MAPPER.readValue(
              OBJECT_MAPPER.writeValueAsString(dummyValue), new TypeReference<Map<String, ?>>() {});
      if (!valueFieldsMap.keySet().containsAll(keyFieldsMap.keySet())) {
        var tmpFieldsMap = Map.copyOf(keyFieldsMap);
        tmpFieldsMap.keySet().removeAll(valueFieldsMap.keySet());
        throw new IllegalArgumentException(
            String.format(
                "specified POJO types are invalid - all fields of the key POJO must also be present in the value POJO\n"
                    + "  - given key fields  : %s\n"
                    + "  - given value fields: %s\n"
                    + "  * missing key fields in value: %s",
                keyFieldsMap.keySet(), valueFieldsMap.keySet(), tmpFieldsMap.keySet()));
      }
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | JsonProcessingException e) {
      throw new RuntimeException(
          "failed to validate serialization constraints for key and value POJO types", e);
    }
  }
}

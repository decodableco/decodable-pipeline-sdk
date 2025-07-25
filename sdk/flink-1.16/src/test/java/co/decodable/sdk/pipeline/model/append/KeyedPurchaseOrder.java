/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.model.append;

import co.decodable.sdk.pipeline.DecodableAppendStreamRecord;
import co.decodable.sdk.pipeline.model.OrderKey;
import co.decodable.sdk.pipeline.model.PurchaseOrder;

public class KeyedPurchaseOrder extends DecodableAppendStreamRecord<OrderKey, PurchaseOrder> {

  // for Jackson
  public KeyedPurchaseOrder() {}

  public KeyedPurchaseOrder(OrderKey key, PurchaseOrder value) {
    super(key, value);
  }
}

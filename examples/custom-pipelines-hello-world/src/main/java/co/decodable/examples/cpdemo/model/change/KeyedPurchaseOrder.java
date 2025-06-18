/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo.model.change;

import co.decodable.examples.cpdemo.model.OrderKey;
import co.decodable.sdk.pipeline.DecodableChangeStreamRecord;

public class KeyedPurchaseOrder
    extends DecodableChangeStreamRecord<OrderKey, PurchaseOrderEnvelope> {

  // for Jackson
  public KeyedPurchaseOrder() {}

  public KeyedPurchaseOrder(OrderKey key, PurchaseOrderEnvelope value) {
    super(key, value);
  }
}

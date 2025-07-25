/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.model.change;

import co.decodable.sdk.pipeline.DecodableChangeStreamRecord;
import co.decodable.sdk.pipeline.model.OrderKey;

public class KeyedPurchaseOrder
    extends DecodableChangeStreamRecord<OrderKey, PurchaseOrderEnvelope> {

  // for Jackson
  public KeyedPurchaseOrder() {}

  public KeyedPurchaseOrder(OrderKey key, PurchaseOrderEnvelope value) {
    super(key, value);
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.model.append;

import co.decodable.sdk.pipeline.DecodableAppendStreamRecord;
import co.decodable.sdk.pipeline.model.PurchaseOrder;

public class KeylessPurchaseOrder extends DecodableAppendStreamRecord<Void, PurchaseOrder> {

  // for Jackson
  public KeylessPurchaseOrder() {}

  public KeylessPurchaseOrder(PurchaseOrder value) {
    super(value);
  }
}

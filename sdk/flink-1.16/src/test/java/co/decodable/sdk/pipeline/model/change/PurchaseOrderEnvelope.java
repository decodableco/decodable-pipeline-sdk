/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.model.change;

import co.decodable.sdk.pipeline.DecodableDebeziumEnvelope;
import co.decodable.sdk.pipeline.model.PurchaseOrder;

public class PurchaseOrderEnvelope extends DecodableDebeziumEnvelope<PurchaseOrder> {

  // for Jackson
  public PurchaseOrderEnvelope() {}

  public PurchaseOrderEnvelope(PurchaseOrder before, PurchaseOrder after, String op, long ts_ms) {
    super(before, after, op, ts_ms);
  }
}

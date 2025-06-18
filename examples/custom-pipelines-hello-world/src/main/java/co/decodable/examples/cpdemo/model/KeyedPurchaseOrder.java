/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo.model;

import co.decodable.sdk.pipeline.DecodableAppendStreamRecord;

public class KeyedPurchaseOrder extends DecodableAppendStreamRecord<OrderKey, PurchaseOrder> {

    //for Jackson
    public KeyedPurchaseOrder() {}

    public KeyedPurchaseOrder(OrderKey key, PurchaseOrder value) {
        super(key, value);
    }

}

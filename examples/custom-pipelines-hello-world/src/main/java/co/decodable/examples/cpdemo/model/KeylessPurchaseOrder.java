/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo.model;

import co.decodable.sdk.pipeline.DecodableAbstractStreamRecord;

public class KeylessPurchaseOrder extends DecodableAbstractStreamRecord<Void, PurchaseOrder> {

    //for Jackson
    public KeylessPurchaseOrder() {}

    public KeylessPurchaseOrder(PurchaseOrder value) {
        super(null, value);
    }

}

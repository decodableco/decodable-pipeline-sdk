/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream.model;

import co.decodable.sdk.pipeline.DecodableChangeStreamRecord;

public class ReviewAggRecord
    extends DecodableChangeStreamRecord<ReviewAggKey, ReviewAggEnvelope> {
  public ReviewAggRecord() {
  }

  public ReviewAggRecord(ReviewAggKey key, ReviewAggEnvelope value) {
    super(key, value);
  }
}

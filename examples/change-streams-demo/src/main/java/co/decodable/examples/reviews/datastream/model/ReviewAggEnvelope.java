/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream.model;

import co.decodable.sdk.pipeline.DecodableDebeziumEnvelope;

public class ReviewAggEnvelope extends DecodableDebeziumEnvelope<ReviewAggValue> {

  public ReviewAggEnvelope() {}

  public ReviewAggEnvelope(ReviewAggValue before, ReviewAggValue after, String op, long ts_ms) {
    super(before, after, op, ts_ms);
  }
}

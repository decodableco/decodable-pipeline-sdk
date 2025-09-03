/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream.model;

import co.decodable.sdk.pipeline.DecodableChangeStreamRecord;

public class ReviewRecord
    extends DecodableChangeStreamRecord<ReviewKey, ReviewEnvelope> {
  public ReviewRecord() {
  }

  public ReviewRecord(ReviewKey key, ReviewEnvelope value) {
    super(key, value);
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream.model;

import java.util.Map;

public class ReviewAggValue {

  public String itemId;
  public long cnt;
  public Map<Long,String> reviewTexts;

  public ReviewAggValue() {
  }

  public ReviewAggValue(String itemId, long cnt, Map<Long,String> reviewTexts) {
    this.itemId = itemId;
    this.cnt = cnt;
    this.reviewTexts = reviewTexts;
  }

}

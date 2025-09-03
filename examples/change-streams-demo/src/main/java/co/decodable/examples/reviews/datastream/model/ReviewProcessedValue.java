/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream.model;

public class ReviewProcessedValue {

  public long id;
  public String itemId;
  public String reviewText;
  public int charCount;

  public ReviewProcessedValue() {
  }

  public ReviewProcessedValue(long id, String itemId, String reviewText, int charCount) {
    this.id = id;
    this.itemId = itemId;
    this.reviewText = reviewText;
    this.charCount = charCount;
  }
}

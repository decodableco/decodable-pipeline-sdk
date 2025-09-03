/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream.model;

public class ReviewValue {

  public long id;
  public String itemId;
  public String reviewText;

  public ReviewValue() {}

  public ReviewValue(long id, String itemId, String reviewText) {
    this.id = id;
    this.itemId = itemId;
    this.reviewText = reviewText;
  }
}

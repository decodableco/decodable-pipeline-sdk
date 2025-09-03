/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.reviews.datastream.model;

public class ReviewAggKey {

  public String itemId;

  public ReviewAggKey() {}

  public ReviewAggKey(String itemId) {
    this.itemId = itemId;
  }
}

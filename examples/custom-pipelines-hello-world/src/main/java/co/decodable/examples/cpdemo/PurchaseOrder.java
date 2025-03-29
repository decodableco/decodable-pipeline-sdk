/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import java.io.Serializable;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseOrder implements Serializable {

  @JsonProperty("order_id")
  public long orderId;

  @JsonProperty("order_date")
  public String orderDate;

  @JsonProperty("customer_name")
  public String customerName;

  public double price;

  @JsonProperty("product_id")
  public long productId;

  @JsonProperty("order_status")
  public boolean orderStatus;
}

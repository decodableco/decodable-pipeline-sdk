/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseOrder {

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

  public PurchaseOrder() {}

  public PurchaseOrder(
      long orderId,
      String orderDate,
      String customerName,
      double price,
      long productId,
      boolean orderStatus) {
    this.orderId = orderId;
    this.orderDate = orderDate;
    this.customerName = customerName;
    this.price = price;
    this.productId = productId;
    this.orderStatus = orderStatus;
  }
}

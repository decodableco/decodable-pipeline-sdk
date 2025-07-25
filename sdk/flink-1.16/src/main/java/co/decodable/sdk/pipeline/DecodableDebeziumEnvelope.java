/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import java.util.Objects;

/**
 * Base class to represent any custom POJO value type wrapped in a Debezium CDC envelope.
 *
 * @param <V> The value data type in a change stream record
 */
@Incubating
public class DecodableDebeziumEnvelope<V> {

  public enum CDC_OP_TYPE {
    C,
    R,
    U,
    D
  }

  private V before;
  private V after;
  private String op;
  private long ts_ms;

  public DecodableDebeziumEnvelope() {}

  public DecodableDebeziumEnvelope(V before, V after, String op, long ts_ms) {
    this.before = before;
    this.after = after;
    this.op = op;
    this.ts_ms = ts_ms;
  }

  public V unwrap() {
    Objects.requireNonNull(this.op, "op field in debezium envelope was null");
    var op = CDC_OP_TYPE.valueOf(this.op.toUpperCase());
    switch (op) {
      case C:
      case R:
      case U:
        return after;
      case D:
        return null;
      default:
        throw new RuntimeException(
            "failed to unwrap debezium envelope for op type '" + op.name() + "'");
    }
  }

  public V getBefore() {
    return before;
  }

  public void setBefore(V before) {
    this.before = before;
  }

  public V getAfter() {
    return after;
  }

  public void setAfter(V after) {
    this.after = after;
  }

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public long getTs_ms() {
    return ts_ms;
  }

  public void setTs_ms(long ts_ms) {
    this.ts_ms = ts_ms;
  }

  @Override
  public String toString() {
    return "DecodableDebeziumEnvelope{"
        + "before="
        + before
        + ", after="
        + after
        + ", op='"
        + op
        + '\''
        + ", ts_ms="
        + ts_ms
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    DecodableDebeziumEnvelope<?> that = (DecodableDebeziumEnvelope<?>) o;
    return ts_ms == that.ts_ms
        && Objects.equals(before, that.before)
        && Objects.equals(after, that.after)
        && Objects.equals(op, that.op);
  }

  @Override
  public int hashCode() {
    return Objects.hash(before, after, op, ts_ms);
  }
}

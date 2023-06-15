/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal;

import co.decodable.sdk.pipeline.DecodableCommittable;

public class DecodableCommittableImpl implements DecodableCommittable {

  private final Object delegate;

  public DecodableCommittableImpl(Object delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.internal;

import co.decodable.sdk.DecodableWriterState;

public class DecodableWriterStateImpl implements DecodableWriterState {

  private final Object delegate;

  public DecodableWriterStateImpl(Object delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }
}

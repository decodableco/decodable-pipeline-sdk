/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/** Denotes that the annotated field is unmodifiable. */
@Target(ElementType.FIELD)
public @interface Unmodifiable {}

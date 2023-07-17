/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.metadata;

import co.decodable.sdk.pipeline.util.Incubating;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes the sink streams accessed by a custom pipeline. Must be specified on the job class in
 * order for the Decodable platform to display the connected streams.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Incubating
public @interface SinkStreams {

  /** One or more sink stream name. */
  String[] value();
}

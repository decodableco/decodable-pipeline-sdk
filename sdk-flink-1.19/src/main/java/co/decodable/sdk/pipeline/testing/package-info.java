/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * Infrastructure and utilities for (integration) testing custom Decodable pipelines.
 *
 * <p>Using a {@link PipelineTestContext}, you can produce elements for one or more Decodable
 * streams, run your custom pipeline, and assert the output elements of this pipeline on another
 * Decodable stream. It is recommended to use <a
 * href="https://testcontainers.com/">Testcontainers</a> for starting a Kafka or Redpanda broker to
 * be used for testing, as shown below:
 *
 * <p>{@snippet class="co.decodable.sdk.pipeline.DataStreamJobTest"
 * region="testing-custom-pipeline"}
 */
package co.decodable.sdk.pipeline.testing;

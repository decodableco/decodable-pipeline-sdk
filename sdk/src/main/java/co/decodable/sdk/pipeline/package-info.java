/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * An SDK for implementing Apache Flink jobs and running them on <a
 * href="https://www.decodable.co">Decodable</a>.
 *
 * <p>The SDK provides {@link org.apache.flink.api.connector.source.Source} and {@link
 * org.apache.flink.api.connector.sink2.StatefulSink} implementations which integrate <a
 * href="https://docs.decodable.co/docs/create-pipelines-using-your-own-apache-flink-jobs">custom
 * Flink jobs</a> seamlessly with Decodable managed streams and, in turn, connections. For instance,
 * you can retrieve the elements of a stream, apply a custom mapping function to them, and write
 * them back to another stream using a Flink job like this:
 *
 * <p>{@snippet class="co.decodable.sdk.pipeline.snippets.DataStreamJob" region="custom-pipeline"}
 */
package co.decodable.sdk.pipeline;

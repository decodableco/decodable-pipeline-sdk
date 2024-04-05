/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
// spotless:off
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
 * <p>{@snippet class="co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob"
 * region="custom-pipeline"}
 *
 * <h2>Stream Metadata</h2>
 *
 * While not required, it is a good practice for custom pipeline authors to provide metadata about
 * the source and sink streams accessed by their pipelines. That way, the referenced pipelines can
 * be displayed in the Decodable user interface. In order to do so, add a file named
 * <i>META-INF/decodable/stream-names.properties</i> to your Flink job JAR. Within that file,
 * specify the name(s) of all source and sink streams as comma-separated lists, using the property
 * keys "source-streams" and "sink-streams":
 *
 * <p>
 * {@snippet :
   source-streams=my_source_stream_1,my_source_stream_2
   sink-streams=my_sink_stream_1,my_sink_stream_2
   }
 * Instead of manually creating this file, it is recommended to generate it automatically, using an
 * annotation processor which ships with this SDK. To do so, specify the stream names using the
 * {@link co.decodable.sdk.pipeline.metadata.SourceStreams} and
 * {@link co.decodable.sdk.pipeline.metadata.SinkStreams} annotations on the job class, as shown in
 * the example listing above.
 *
 * <h2>Operator naming convention</h2>
 *
 * Optionally, naming operators with a prefix of {@code [stream-<stream_name>]} will allow them to
 * properly display input and output metrics by stream when run. For example, a stream source operator
 * with the name {@code [stream-my_source_stream_1] Purchase Order Source} or a stream sink operator with the name
 * {@code [stream-my_sink_stream_1] Purchase Order Sink} would generate metrics for that source and sink stream
 * respectively.
 *
 * <h2>Custom Metrics</h2>
 *
 * By default, Decodable custom pipelines expose a set of Flink metrics. To expose additional metrics of your job,
 * add the DecodableMetrics metric group to your registered metric:
 *
 * <p>{@snippet class = "co.decodable.sdk.pipeline.snippets.PurchaseOrderProcessingJob"
 * region = "metric-group"}
 */
//spotless:on
package co.decodable.sdk.pipeline;

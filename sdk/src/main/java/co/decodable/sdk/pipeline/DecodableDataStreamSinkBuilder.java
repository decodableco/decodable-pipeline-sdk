/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;

@Incubating
public class DecodableDataStreamSinkBuilder<T> {
  private DataStream<T> dataStream;
  private DecodableStreamSinkBuilder decodableStreamSinkBuilder;
  private String streamId;
  private String streamName;

  public DecodableDataStreamSinkBuilder(DataStream<T> dataStream) {
    this.dataStream = dataStream;
    this.decodableStreamSinkBuilder = DecodableStreamSink.<T>builder();
  }

  public DecodableDataStreamSinkBuilder<T> withStreamName(String streamName) {
    this.streamName = streamName;
    decodableStreamSinkBuilder.withStreamName(streamName);
    return this;
  }

  public DecodableDataStreamSinkBuilder<T> withStreamId(String streamId) {
    this.streamId = streamId;
    decodableStreamSinkBuilder.withStreamId(streamId);
    return this;
  }

  public DecodableDataStreamSinkBuilder<T> withSerializationSchema(
      SerializationSchema<T> serializationSchema) {
    decodableStreamSinkBuilder.withSerializationSchema(serializationSchema);
    return this;
  }

  public DataStreamSink<T> build() {
    var name =
        streamId == null
            ? String.format("[decodable-pipeline-sdk-stream-name] %s", streamName)
            : String.format("[decodable-pipeline-sdk-stream-id] %s", streamId);
    return dataStream.sinkTo(decodableStreamSinkBuilder.build()).name(name);
  }
}

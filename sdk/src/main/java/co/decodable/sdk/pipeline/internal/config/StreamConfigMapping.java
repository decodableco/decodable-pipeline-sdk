/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline.internal.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamConfigMapping {

  private static final Pattern KEY_PATTERN = Pattern.compile("DECODABLE_STREAM_CONFIG_(.*)");

  private final Map<String, StreamConfig> configsByStreamName = new HashMap<>();

  public StreamConfigMapping(Map<String, String> environment) {
    ObjectMapper mapper = new ObjectMapper();

    for (Entry<String, String> entry : environment.entrySet()) {
      Matcher keyMatcher = KEY_PATTERN.matcher(entry.getKey());
      if (keyMatcher.matches()) {
        String streamId = keyMatcher.group(1);

        try {
          Map<String, Object> config =
              mapper.readValue(entry.getValue(), new TypeReference<Map<String, Object>>() {});
          String streamName = (String) config.get("name");

          @SuppressWarnings("unchecked")
          StreamConfig streamConfig =
              new StreamConfig(
                  streamId, streamName, (Map<String, String>) config.get("properties"));
          configsByStreamName.put(streamName, streamConfig);
        } catch (JsonProcessingException e) {
          throw new IllegalArgumentException(
              String.format("Couldn't parse stream configuration env variable %s", entry.getKey()),
              e);
        }
      }
    }
  }

  public StreamConfig determineConfig(String streamName) {
    StreamConfig streamConfig = null;

    if (streamName != null) {
      streamConfig = configsByStreamName.get(streamName);
      if (streamConfig == null) {
        throw new IllegalStateException(
            String.format(
                "No topic name could be determined for stream with name '%s'", streamName));
      }
    } else {
      throw new IllegalStateException("Stream name must be specified");
    }

    return streamConfig;
  }
}

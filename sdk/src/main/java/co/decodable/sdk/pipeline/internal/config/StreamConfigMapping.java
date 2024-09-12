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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamConfigMapping {

  private static final Pattern KEY_PATTERN = Pattern.compile("DECODABLE_STREAM_CONFIG_(.*)");
  private static final Set<String> SECRET_PROPERTY_KEYS =
      Set.of(
          "properties.ssl.keystore.key",
          "properties.ssl.key.password",
          "properties.ssl.truststore.password",
          "properties.sasl.jaas.config",
          "properties.ssl.truststore.certificates",
          "properties.ssl.keystore.certificate.chain");

  private final Map<String, StreamConfig> configsByStreamName = new HashMap<>();
  private final Map<String, StreamConfig> configsByStreamId = new HashMap<>();

  public StreamConfigMapping(Map<String, String> environment) {
    ObjectMapper mapper = new ObjectMapper();

    for (Entry<String, String> entry : environment.entrySet()) {
      Matcher keyMatcher = KEY_PATTERN.matcher(entry.getKey());
      if (keyMatcher.matches()) {
        String streamId = keyMatcher.group(1);

        Map<String, Object> config;
        try {
          config = mapper.readValue(entry.getValue(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
          throw new IllegalArgumentException(
              String.format("Couldn't parse stream configuration env variable %s", entry.getKey()),
              e);
        }

        String streamName = (String) config.get("name");
        Map<String, Object> properties = (Map<String, Object>) config.get("properties");

        for (String secretKey : SECRET_PROPERTY_KEYS) {
          properties.computeIfPresent(
              secretKey,
              (k, v) -> {
                try {
                  return Files.readString(Paths.get((String) v));
                } catch (IOException e) {
                  throw new IllegalArgumentException(
                      String.format(
                          "Could not read secret property key %s from path %s",
                          secretKey, (String) v),
                      e);
                }
              });
        }

        @SuppressWarnings("unchecked")
        StreamConfig streamConfig =
            new StreamConfig(streamId, streamName, (Map<String, String>) config.get("properties"));
        configsByStreamId.put(streamId, streamConfig);
        configsByStreamName.put(streamName, streamConfig);
      }
    }
  }

  public StreamConfig determineConfig(String streamName, String streamId) {
    StreamConfig streamConfig = null;

    if (streamName != null) {
      if (streamId != null) {
        throw new IllegalStateException("Only one of stream name or stream id may be specified");
      } else {
        streamConfig = configsByStreamName.get(streamName);
        if (streamConfig == null) {
          throw new IllegalStateException(
              String.format(
                  "No topic name could be determined for stream with name '%s'", streamName));
        }
      }
    } else {
      if (streamId != null) {
        streamConfig = configsByStreamId.get(streamId);
        if (streamConfig == null) {
          throw new IllegalStateException(
              String.format("No topic name could be determined for stream with id '%s'", streamId));
        }

      } else {
        throw new IllegalStateException("Either stream name or stream id must be specified");
      }
    }

    return streamConfig;
  }
}

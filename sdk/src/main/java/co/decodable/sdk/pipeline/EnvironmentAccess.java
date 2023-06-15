/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk.pipeline;

import co.decodable.sdk.pipeline.util.Incubating;
import java.util.Map;
import java.util.Objects;

@Incubating
public class EnvironmentAccess {

  private static Environment ENVIRONMENT = new SystemEnvironment();

  private EnvironmentAccess() {}

  public static synchronized void setEnvironment(Environment environment) {
    Objects.requireNonNull(environment, "Environment must not be null");
    ENVIRONMENT = environment;
  }

  public static synchronized void resetEnvironment() {
    ENVIRONMENT = new SystemEnvironment();
  }

  public static synchronized Environment getEnvironment() {
    return ENVIRONMENT;
  }

  public interface Environment {
    Map<String, String> getEnvironmentConfiguration();
  }

  private static class SystemEnvironment implements Environment {

    @Override
    public Map<String, String> getEnvironmentConfiguration() {
      return System.getenv();
    }
  }
}

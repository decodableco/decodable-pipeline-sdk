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

/**
 * Provides access to the environment from within a custom Flink job. By default, the system
 * environment is exposed. For testing purposes, a custom environment can be set.
 */
@Incubating
public class EnvironmentAccess {

  private static Environment ENVIRONMENT = new SystemEnvironment();

  private EnvironmentAccess() {}

  /** Sets the given environment as the active one. */
  public static synchronized void setEnvironment(Environment environment) {
    Objects.requireNonNull(environment, "Environment must not be null");
    ENVIRONMENT = environment;
  }

  /** Resets active the environment to the system environment. */
  public static synchronized void resetEnvironment() {
    ENVIRONMENT = new SystemEnvironment();
  }

  /** Returns the current environment. */
  public static synchronized Environment getEnvironment() {
    return ENVIRONMENT;
  }

  /** Exposes the environment variables of the current process. */
  public interface Environment {
    /** Returns the current environment variables, keyed by name. */
    Map<String, String> getEnvironmentConfiguration();
  }

  private static class SystemEnvironment implements Environment {

    @Override
    public Map<String, String> getEnvironmentConfiguration() {
      return System.getenv();
    }
  }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.sdk;

import co.decodable.sdk.util.Incubating;
import java.util.Collections;
import java.util.Map;

@Incubating
public class Environment {

  private static Map<String, String> ENVIRONMENT;

  public static synchronized void setEnvironmentConfiguration(Map<String, String> environment) {
    ENVIRONMENT = Collections.unmodifiableMap(environment);
  }

  public static synchronized void resetEnvironmentConfiguration() {
    ENVIRONMENT = null;
  }

  public static synchronized Map<String, String> getEnvironmentConfiguration() {
    return ENVIRONMENT != null ? ENVIRONMENT : System.getenv();
  }
}

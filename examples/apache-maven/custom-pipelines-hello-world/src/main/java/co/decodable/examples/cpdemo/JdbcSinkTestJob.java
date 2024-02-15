/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.examples.cpdemo;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

import co.decodable.sdk.pipeline.DecodableSecret;

public class JdbcSinkTestJob {

	public static void main(String[] args) throws Exception {
		EnvironmentSettings settings = EnvironmentSettings.inStreamingMode();
		TableEnvironment tEnv = TableEnvironment.create(settings);

		String host = "...";
		String password = DecodableSecret.withName("gm_PG_PW").value();

		tEnv.executeSql("CREATE TABLE products (\n"
				+ "  id BIGINT,\n"
				+ "  name STRING NOT NULL,\n"
				+ "  description STRING,\n"
				+ "  PRIMARY KEY (id) NOT ENFORCED\n"
				+ ") WITH (\n"
				+ "   'connector' = 'jdbc',\n"
				+ "   'url' = 'jdbc:postgresql://" + host + "',\n"
				+ "   'username' = 'postgres',\n"
				+ "   'password' = '" + password + "',\n"
				+ "   'table-name' = 'gm_jdbc_test.products'\n"
				+ ");");

		tEnv.executeSql("INSERT INTO products SELECT 1, 'test product', 'fancy product'");
	}
}

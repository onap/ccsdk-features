/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.ExtRestClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.InvalidProtocolException;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo.Protocol;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.IndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.CreateIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.IndexResponse;

public class TestDatabaseClient {


	private static ExtRestClient client;

	@BeforeClass
	public static void init() throws InvalidProtocolException {

		 client = ExtRestClient.createInstance("localhost",9200,Protocol.HTTP);

	}
	@AfterClass
	public static void deinit() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    @Ignore
	@Test
	public void testIndexExists() {

		 GetIndexRequest request =new GetIndexRequest("mwtn");
		 try {
				boolean response = client.indicesExists(request);
				assertTrue(response);
			} catch (IOException e) {
				fail(e.getMessage());
			}
		 request =new GetIndexRequest("mwrn");
		 try {
				boolean response = client.indicesExists(request);
				assertFalse(response);
			} catch (IOException e) {
				fail(e.getMessage());
			}

	}
    @Ignore
	@Test
	public void testIndexCreate() {
		CreateIndexRequest request = new CreateIndexRequest("mwtn");
		try {
			CreateIndexResponse response = client.createIndex(request);
			assertTrue(response.isAcknowledged());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
    @Ignore
	@Test
	public void testAddPmEntry() {
		String json="{\"node-name\":\"sim12600\",\"uuid-interface\":\"LP-MWPS-TTP-01\",\"layer-protocol-name\":\"MWPS\",\"radio-signal-id\":\"Test11\",\"time-stamp\":\"2017-07-04T00:00:00.0Z\",\"granularity-period\":\"PERIOD_24HOURS\",\"scanner-id\":\"PM_RADIO_24H_1\",\"performance-data\":{\"rx-level-avg\":-41,\"time2-states\":-1,\"time4-states-s\":9,\"time4-states\":0,\"time8-states\":0,\"time16-states-s\":-1,\"time16-states\":0,\"time32-states\":1,\"time64-states\":1,\"time128-states\":2,\"time256-states\":38319,\"time512-states\":-1,\"time512-states-l\":-1,\"time1024-states\":-1,\"time1024-states-l\":-1,\"time2048-states\":-1,\"time2048-states-l\":-1,\"time4096-states\":-1,\"time4096-states-l\":-1,\"time8192-states\":-1,\"time8192-states-l\":-1,\"snir-min\":-99,\"unavailability\":504,\"tx-level-max\":25,\"tx-level-avg\":25,\"rx-level-min\":-41,\"rx-level-max\":-41,\"ses\":2,\"tx-level-min\":20,\"snir-max\":-99,\"snir-avg\":-99,\"xpd-min\":-99,\"xpd-max\":-99,\"xpd-avg\":-99,\"rf-temp-min\":-99,\"rf-temp-max\":-99,\"rf-temp-avg\":-99,\"defect-blocks-sum\":-1,\"time-period\":86400,\"cses\":0,\"es\":5},\"suspect-interval-flag\":true}";
		IndexRequest request=new IndexRequest("historicalperformance24h", "historicalperformance24h","sim12600/LP-MWPS-TTP-01/2017-07-04T00:00:00.0+00:00");
		request.source(json);
		try {
			IndexResponse response = client.index(request);

			assertTrue(response.isCreated() || response.isUpdated());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}

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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;

public class TestDbClient {
	
	private static HtDatabaseClient dbClient;
	private static HostInfo[] hosts = new HostInfo[] { new HostInfo("localhost", Integer
			.valueOf(System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200")) };

	@BeforeClass
	public static void init() {

		dbClient = new HtDatabaseClient(hosts);
		dbClient.waitForYellowStatus(20000);

	}
	@Test
	public void testCRUD() {
		final String IDX = "test23-knmoinsd";
		final String ID = "abcddd";
		final String JSON = "{\"data\":{\"inner\":\"more\"}}";
		final String JSON2 = "{\"data\":{\"inner\":\"more2\"}}";
		//Create
		String esId=dbClient.doWriteRaw(IDX, ID, JSON);
		assertEquals("inserted id is wrong",ID,esId);
		//Read
		SearchResult<SearchHit> result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
		assertEquals("amount of results is wrong",1,result.getTotal());
		assertEquals("data not valid", JSON,result.getHits().get(0).getSourceAsString());
		//Update
		esId= dbClient.doUpdateOrCreate(IDX, ID, JSON2);
		assertEquals("update response not successfull",ID,esId);
		//Verify
		result = dbClient.doReadByQueryJsonData( IDX, QueryBuilders.matchQuery("_id", ID));
		assertEquals("amount of results is wrong",1,result.getTotal());
		assertEquals("data not valid", JSON2,result.getHits().get(0).getSourceAsString());
		//Delete
		boolean del=dbClient.doRemove(IDX, ID);
		assertTrue("item not deleted",del);
		//Verify
		result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
		assertEquals("amount of results is wrong",0,result.getTotal());
	}

}

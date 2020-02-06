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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.HtDatabaseEventsService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.HtDatabaseMaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.MaintenanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.MaintenanceEntity;

/**
 * - Handling of inital values for Maintenance mode.
 */
public class TestMaintenanceService {
	
	private static ElasticSearchDataProvider dbProvider;
	private static HtDatabaseClient dbRawProvider;
	private static HtDatabaseMaintenance service = null;

	private static final String NODEID = "node1";
	private static final String NODEID2 = "node2";
	@BeforeClass
	public static void init() throws Exception {

		dbProvider = new ElasticSearchDataProvider(TestCRUDforDatabase.hosts);
		dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
		dbRawProvider = new HtDatabaseClient(TestCRUDforDatabase.hosts);
		service  = dbProvider.getHtDatabaseMaintenance();
	}
	@Test
	public void test() {
		clearDbEntity(Entity.Maintenancemode);
		MaintenanceEntity obj = service.createIfNotExists(NODEID);
		assertNotNull(obj);
		obj = service.createIfNotExists(NODEID2);
		assertNotNull(obj);
		obj = service.getMaintenance(NODEID);
		assertNotNull(obj);
		List<MaintenanceEntity> list = service.getAll();
		assertEquals(2,list.size());
		service.deleteIfNotRequired(NODEID);
		obj = service.getMaintenance(NODEID);
		assertNull(obj);
		
		
		obj = service.setMaintenance(createMaintenance(NODEID,true));
		
		
	}
	/**
	 * @param nodeId 
	 * @param active 
	 * @return
	 */
	private static MaintenanceEntity createMaintenance(String nodeId, Boolean active) {
		return new MaintenanceBuilder().setNodeId(nodeId).setActive(active).setProblem("problem").setObjectIdRef("idref").build();
	}
	private static void clearDbEntity(Entity entity) {
		DeleteByQueryRequest query = new DeleteByQueryRequest(entity.getName());
		query.setQuery(QueryBuilders.matchAllQuery().toJSON());
		try {
			dbRawProvider.deleteByQuery(query);
		} catch (IOException e) {
			e.printStackTrace();
		}
		TestCRUDforDatabase.trySleep(1000);
	}

}

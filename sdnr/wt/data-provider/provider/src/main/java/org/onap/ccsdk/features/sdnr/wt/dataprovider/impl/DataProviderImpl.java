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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.ReadyHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProviderImpl implements IEntityDataProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderImpl.class);

    private static final String APPLICATION_NAME = null;
    private RpcProviderService rpcProviderService = null;
    private DataProviderServiceImpl rpcApiService;
	private ReadyHttpServlet readyServlet;
    private HtDatabaseClient dbClient;


    // Blueprint 1
    public DataProviderImpl() {
        LOG.info("Creating provider for {}", APPLICATION_NAME);
    }

    public void setRpcProviderService(RpcProviderService rpcProviderService) {
        this.rpcProviderService = rpcProviderService;
    }
    public void setReadyServlet(ReadyHttpServlet readyServlet) {
    	this.readyServlet = readyServlet;
    }
    public void init() throws Exception {

        LOG.info("Session Initiated start {}", APPLICATION_NAME);

        // Start RPC Service
        this.rpcApiService = new DataProviderServiceImpl(rpcProviderService);
        // Get configuration

        LOG.info("Session Initiated end. Initialization done");
    }

    @Override
    public void close() throws Exception {
        LOG.info("DeviceManagerImpl closing ...");

        close(dbClient);
        close(rpcApiService);
        LOG.info("DeviceManagerImpl closing done");
    }

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     *
     * @param toClose
     * @throws Exception
     */
    private void close(AutoCloseable... toCloseList) throws Exception {
        for (AutoCloseable element : toCloseList) {
            if (element != null) {
                element.close();
            }
        }
    }

    @Override
    public DataProvider getDataProvider() {
        return rpcApiService.getDataProvider();
    }

	@Override
	public void setReadyStatus(boolean status) {
		if(this.readyServlet!=null) {
			this.readyServlet.setStatus(status);
		}
	}
}

/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.UserdataHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about.AboutHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.StatusChangedHandler.StatusKey;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = IEntityDataProvider.class, immediate = true)
public class DataProviderImpl implements IEntityDataProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderImpl.class);

    private static final String APPLICATION_NAME = "data-provider";

    private final RpcProviderService rpcProviderService;
    private DataProviderServiceImpl rpcApiService;
    private final AboutHttpServlet aboutServlet;
    private final DataBroker dataBroker;

    @Activate
    public DataProviderImpl(@Reference final RpcProviderService rpcProviderService,
                            @Reference final AboutHttpServlet aboutServlet,
                            @Reference final DataBroker dataBroker) {
        super();
        LOG.info("Creating provider for {}", APPLICATION_NAME);
        this.rpcProviderService = rpcProviderService;
        this.aboutServlet = aboutServlet;
        this.dataBroker = dataBroker;

        LOG.info("Session Initiated start {}", APPLICATION_NAME);
        // Start RPC Service
        this.rpcApiService = new DataProviderServiceImpl(rpcProviderService, this.dataBroker);
        // Wire data sources into Whiteboard-managed servlets
        DataTreeHttpServlet.setInventoryTreeProvider(this.rpcApiService.getInventoryTreeProvider());
        UserdataHttpServlet.setDatabaseClient(this.rpcApiService.getHtDatabaseUserManager());
        LOG.info("Session Initiated end. Initialization done");
    }

    @Deactivate
    @Override
    public void close() throws Exception {
        LOG.info("DeviceManagerImpl closing ...");
        close(rpcApiService);
        LOG.info("DeviceManagerImpl closing done");
    }

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     *
     * @param toCloseList list of elements to close
     * @throws Exception if closing fails
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
    public HtDatabaseMaintenance getHtDatabaseMaintenance() {
        return rpcApiService.getHtDatabaseMaintenance();
    }

    @Override
    public NetconfTimeStamp getConverter() {
        return NetconfTimeStampImpl.getConverter();
    }

    @Override
    public void setStatus(StatusKey key, String value) {
        if (this.aboutServlet != null) {
            if (key == StatusKey.CLUSTER_SIZE) {
                this.aboutServlet.setClusterSize(value);
            }
        }
    }

    @Override
    public HtUserdataManager getHtDatabaseUserManager() {
        return this.rpcApiService.getHtDatabaseUserManager();
    }

}

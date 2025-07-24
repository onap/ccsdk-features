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

import javax.servlet.ServletException;
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
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProviderImpl implements IEntityDataProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderImpl.class);

    private static final String APPLICATION_NAME = "data-provider";
    private RpcProviderService rpcProviderService = null;
    private DataProviderServiceImpl rpcApiService;
    private AboutHttpServlet aboutServlet;
    private DataTreeHttpServlet treeServlet;
    private UserdataHttpServlet userdataServlet;
    private DataBroker dataBroker;

    // Blueprint 1
    public DataProviderImpl() {
        super();
        LOG.info("Creating provider for {}", APPLICATION_NAME);
    }

    @SuppressWarnings("unused")
    public void setRpcProviderService(RpcProviderService rpcProviderService) {
        this.rpcProviderService = rpcProviderService;
    }

    @SuppressWarnings("unused")
    public void setAboutServlet(AboutHttpServlet aboutServlet) {
        this.aboutServlet = aboutServlet;
    }

    @SuppressWarnings("unused")
    public void setTreeServlet(DataTreeHttpServlet treeServlet) {
        this.treeServlet = treeServlet;
    }

    @SuppressWarnings("unused")
    public void setUserdataServlet(UserdataHttpServlet userdataServlet) {
        this.userdataServlet = userdataServlet;
    }

    @SuppressWarnings("unused")
    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @SuppressWarnings("unused")
    public void init() {

        LOG.info("Session Initiated start {}", APPLICATION_NAME);
        // Start RPC Service
        this.rpcApiService = new DataProviderServiceImpl(rpcProviderService, this.dataBroker);
        this.treeServlet.setInventoryTreeProvider(this.rpcApiService.getInventoryTreeProvider());
        this.userdataServlet.setDatabaseClient(this.rpcApiService.getHtDatabaseUserManager());
        LOG.info("Session Initiated end. Initialization done");

    }

    @SuppressWarnings("unused")
    public void onUnbindService(HttpService httpService) {
        httpService.unregister(AboutHttpServlet.URI_PRE);
        httpService.unregister(DataTreeHttpServlet.URI_PRE);
        this.aboutServlet = null;
        this.treeServlet = null;
    }

    @SuppressWarnings("unused")
    public void onBindService(HttpService httpService)
            throws ServletException, NamespaceException {
        if (httpService == null) {
            LOG.warn("Unable to inject HttpService into loader.");
        } else {
            httpService.registerServlet(AboutHttpServlet.URI_PRE, aboutServlet, null, null);
            httpService.registerServlet(DataTreeHttpServlet.URI_PRE, treeServlet, null, null);
            LOG.info("about servlet and tree servlet registered.");
        }
    }

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

/*
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
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.FactoryRegistration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerOnfImpl implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerOnfImpl.class);
    private static final String APPLICATIONNAME = "DeviceManagerOnf";

    private NetconfNetworkElementService netconfNetworkElementService;

    private Boolean devicemanagerInitializationOk = false;
    private FactoryRegistration<ONFCoreNetworkElementFactory> resOnf;
    private DeviceManagerOnfConfiguration configuration;

    // Blueprint begin
    public DeviceManagerOnfImpl() {
        LOG.info("Creating provider for {}", APPLICATIONNAME);
        resOnf = null;
    }

    public void setNetconfNetworkElementService(NetconfNetworkElementService netconfNetworkElementService) {
        this.netconfNetworkElementService = netconfNetworkElementService;
    }

    public void init() throws Exception {

        LOG.info("Session Initiated start {}", APPLICATIONNAME);

        configuration = new DeviceManagerOnfConfiguration(
                netconfNetworkElementService.getServiceProvider().getConfigurationFileRepresentation());
        resOnf = netconfNetworkElementService
                .registerBindingNetworkElementFactory(new ONFCoreNetworkElementFactory(configuration));


        netconfNetworkElementService.writeToEventLog(APPLICATIONNAME, "startup", "done");
        this.devicemanagerInitializationOk = true;

        LOG.info("Session Initiated end. Initialization done {}", devicemanagerInitializationOk);
    }
    // Blueprint end

    @Override
    public void close() throws Exception {
        LOG.info("closing ...");
        close(resOnf);
        LOG.info("closing done");
    }

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     *
     * @param toCloseList
     * @throws Exception
     */
    private void close(AutoCloseable... toCloseList) {
        for (AutoCloseable element : toCloseList) {
            if (element != null) {
                try {
                    element.close();
                } catch (Exception e) {
                    LOG.warn("Fail during close: ", e);
                }
            }
        }
    }
}

/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.DeviceManagerOnf14Impl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;

public class TestDeviceManagerOnf14Impl extends Mockito {

    DeviceManagerOnf14Impl devMgrOnf14;
    private static NetconfNetworkElementService netconfElemService;

    @Before
    public void init() throws InterruptedException, IOException {
        netconfElemService = mock(NetconfNetworkElementService.class);
    }

    @Test
    public void test() throws Exception {
        devMgrOnf14 = new DeviceManagerOnf14Impl();

        try {
            devMgrOnf14.setNetconfNetworkElementService(netconfElemService);
            devMgrOnf14.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanUp() throws Exception {
        devMgrOnf14.close();
    }

}

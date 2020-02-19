/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc;

import org.junit.Before;
import org.junit.Test;

import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.OofpcipocClient;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.OofpcipocProvider;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.math.*;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdOutputBuilder;
import com.google.common.util.concurrent.ListenableFuture;



public class TestOofpcipoc extends AbstractConcurrentDataBrokerTest {

    private OofpcipocProvider oofpcipocProvider;
    private static final Logger LOG = LoggerFactory.getLogger(OofpcipocProvider.class);

    @Before
    public void setUp() throws Exception {
        if (null == oofpcipocProvider) {
            DataBroker dataBroker = getDataBroker();
            NotificationPublishService mockNotification = mock(NotificationPublishService.class);
            RpcProviderRegistry mockRpcRegistry = mock(RpcProviderRegistry.class);
            OofpcipocClient mockSliClient = mock(OofpcipocClient.class);
            oofpcipocProvider = new OofpcipocProvider(dataBroker, mockNotification, mockRpcRegistry, mockSliClient);
        }
    }

    //Testcase should return error 503 when No service logic active for ConfigurationPhyCellId
    @Test
    public void testConfigurationPhyCellId() {

        ConfigurationPhyCellIdInputBuilder inputBuilder = new ConfigurationPhyCellIdInputBuilder();

        inputBuilder.setFapServiceNumberOfEntries(new BigInteger("1"));

        // TODO: currently initialize SvcLogicServiceClient is failing, need to fix
        ListenableFuture<RpcResult<ConfigurationPhyCellIdOutput>> future = oofpcipocProvider
                                                                          .configurationPhyCellId(inputBuilder.build());
        RpcResult<ConfigurationPhyCellIdOutput> rpcResult = null;
        try {
            rpcResult = future.get();
        } catch (Exception e) {
            fail("Error : " + e);
        }
        LOG.info("result: {}", rpcResult);
        assertEquals("503", rpcResult.getResult().getResponseCode());
    }

    //Input parameter validation
    @Test
    public void testConfigurationPhyCellIdValidation() {

        ListenableFuture<RpcResult<ConfigurationPhyCellIdOutput>> future = oofpcipocProvider
                                                                                      .configurationPhyCellId(null);
        RpcResult<ConfigurationPhyCellIdOutput> rpcResult = null;
        try {
            rpcResult = future.get();
        } catch (Exception e) {
            fail("Error : " + e);
        }
        LOG.info("result: {}", rpcResult);
        assertEquals("Input is null", rpcResult.getResult().getResponseCode());
    }
}

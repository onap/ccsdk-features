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

package org.onap.ccsdk.features.sdnr.northbound.cmnotify;

import org.junit.Before;
import org.junit.Test;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationOutput;

import com.google.common.util.concurrent.ListenableFuture;



public class TestCMNotify {

    private CMNotifyProvider cMNotifyProvider;
    private static final Logger LOG = LoggerFactory.getLogger(CMNotifyProvider.class);

    @Before
    public void setUp() throws Exception {
        if (null == cMNotifyProvider) {
            DataBroker dataBroker = mock(DataBroker.class);
            NotificationPublishService mockNotification = mock(NotificationPublishService.class);
            RpcProviderService mockRpcRegistry = mock(RpcProviderService.class);
            CMNotifyClient mockSliClient = mock(CMNotifyClient.class);
            cMNotifyProvider = new CMNotifyProvider();
            cMNotifyProvider.setDataBroker(dataBroker);
            cMNotifyProvider.setNotificationPublishService(mockNotification);
            cMNotifyProvider.setRpcProviderService(mockRpcRegistry);
            cMNotifyProvider.setClient(mockSliClient);
        }
    }

    //Testcase should return error 503 when No service logic active for NbrlistChangeNotification
    @Test
    public void testNbrlistChangeNotification() {

        NbrlistChangeNotificationInputBuilder inputBuilder = new NbrlistChangeNotificationInputBuilder();

        inputBuilder.setFapServiceNumberOfEntriesChanged(Uint64.valueOf("1"));

        // TODO: currently initialize SvcLogicServiceClient is failing, need to fix
        ListenableFuture<RpcResult<NbrlistChangeNotificationOutput>> future = cMNotifyProvider
                                                                          .nbrlistChangeNotification(inputBuilder.build());
        RpcResult<NbrlistChangeNotificationOutput> rpcResult = null;
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
    public void testvValidation() {

        ListenableFuture<RpcResult<NbrlistChangeNotificationOutput>> future = cMNotifyProvider
                                                                                      .nbrlistChangeNotification(null);
        RpcResult<NbrlistChangeNotificationOutput> rpcResult = null;
        try {
            rpcResult = future.get();
        } catch (Exception e) {
            fail("Error : " + e);
        }
        LOG.info("result: {}", rpcResult);
        assertEquals("Input is null", rpcResult.getResult().getResponseCode());
    }
}

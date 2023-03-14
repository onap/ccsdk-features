/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.client;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.fault.FaultNotificationClient;

public class TestFaultNotificationClient extends FaultNotificationClient {

    public static String baseUrl = "http://localhost:8181";
    FaultNotificationClient testClient;


    public TestFaultNotificationClient() {
        super(baseUrl);

    }

    @Test
    public void testFaultNotificationClient() {
        testClient = new TestFaultNotificationClient();
        testClient.setAuthorization("admin", "admin");
        Map<String, String> payloadMap = FaultNotificationClient.createFaultNotificationPayloadMap(
                "TEST_50001", "1", "2019-11-20T09:25:19.948Z",
                "SEDNKSAHQ01M01nSky01", "lossOfSignal", "Critical");
        String msg = testClient.prepareMessageFromPayloadMap(payloadMap);
        assertTrue(testClient.sendNotification(msg));
        assertTrue(testClient.sendNotification(msg));
    }

    @Override
    @Nonnull
    public BaseHTTPResponse sendRequest(String uri, String method, String body, Map<String, String> headers)
            throws IOException {
        System.out.println("In overridden sendRequest in TestFaultNotificationClient");
        return new BaseHTTPResponse(200, body);
    }
}

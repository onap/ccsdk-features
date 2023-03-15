/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
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

import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm.CMBasicHeaderFieldsNotification;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm.CMNotification;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm.CMNotificationClient;

public class TestCMNotificationClient extends CMNotificationClient {
    public static String baseUrl = "http://localhost:8181";
    CMNotificationClient testClient;

    public TestCMNotificationClient() {
        super(baseUrl);
    }

    @Test
    public void testCMNotificationClient() {
        testClient = new TestCMNotificationClient();
        testClient.setAuthorization("admin", "admin");

        String msg = testClient.prepareMessageFromPayloadMap(
            CMNotificationClient.createCMNotificationPayloadMap(
                CMNotification.builder()
                    .withCMBasicHeaderFieldsNotification(CMBasicHeaderFieldsNotification.builder()
                        .withCMNodeId("test-node")
                        .withCMSequence("1")
                        .withCMOccurrenceTime("2021-10-18T15:25:19.948Z")
                        .withSourceId("src_device_id_1732")
                        .withNotificationType("notifyMOIChanges")
                        .build())
                    .withCMNotificationId("123")
                    .withCMSourceIndicator("UNKNOWN")
                    .withCMPath("http://samsung.com/ves=1")
                    .withCMOperation("CREATE")
                    .withCMValue("value")
                    .build()
            ));
        assertTrue(testClient.sendNotification(msg));
    }

    @Override
    @Nonnull
    public BaseHTTPResponse sendRequest(String uri, String method, String body, Map<String, String> headers) {
        System.out.println("In overridden sendRequest in TestCMNotificationClient");
        return new BaseHTTPResponse(200, body);
    }
}
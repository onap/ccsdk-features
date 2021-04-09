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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.PNFMountPointClient;

public class TestPNFMountPointClient extends PNFMountPointClient {

    public static String baseUrl = "http://localhost:8181";
    PNFMountPointClient testClient;


    public TestPNFMountPointClient() {
        super(baseUrl);

    }

    @Test
    public void testPNFMountPointClient() {
        testClient = new TestPNFMountPointClient();
        testClient.setAuthorization("admin", "admin");
        assertEquals(true,
                testClient.pnfMountPointCreate("TEST 50001", "127.0.0.1", "TLS", "key_id", "admin", "admin", "17380"));

        assertEquals(true,
                testClient.pnfMountPointCreate("TEST_50001", "127.0.0.1", "SSH", "key_id", "admin", "admin", "17380"));
    }

    @Override
    @Nonnull
    public BaseHTTPResponse sendRequest(String uri, String method, String body, Map<String, String> headers)
            throws IOException {
        System.out.println("In overridden sendRequest in TestPNFMountPointClient, uri = "+uri);
        return new BaseHTTPResponse(200, body);
    }

}

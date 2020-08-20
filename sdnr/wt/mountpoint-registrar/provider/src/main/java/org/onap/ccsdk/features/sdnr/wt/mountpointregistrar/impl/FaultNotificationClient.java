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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaultNotificationClient extends BaseHTTPClient {

    private static final Logger LOG = LoggerFactory.getLogger(FaultNotificationClient.class);
    private static final String FAULT_NOTIFICATION_URI = "restconf/operations/devicemanager:push-fault-notification";
    private final Map<String, String> headerMap;

    // @formatter:off
    private static final String FAULT_PAYLOAD = "{\n"
            + "  \"devicemanager:input\": {\n"
            + "    \"devicemanager:node-id\": \"@node-id@\",\n"
            + "    \"devicemanager:counter\": \"@counter@\",\n"
            + "    \"devicemanager:timestamp\": \"@timestamp@\",\n"
            + "    \"devicemanager:object-id\": \"@object-id@\",\n"
            + "    \"devicemanager:problem\": \"@problem@\",\n"
            + "    \"devicemanager:severity\": \"@severity@\"\n"
            + "  }\n"
            + "}";
    // @formatter:on


    public FaultNotificationClient(String baseUrl) {
        super(baseUrl);

        this.headerMap = new HashMap<>();
        this.headerMap.put("Content-Type", "application/json");
        this.headerMap.put("Accept", "application/json");
    }

    public void setAuthorization(String username, String password) {
        String credentials = username + ":" + password;
        this.headerMap.put("Authorization", "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes())));

    }

    public boolean sendFaultNotification(String faultNodeId, String faultCounter, String faultOccurrenceTime,
            String faultObjectId, String faultReason, String faultSeverity) {
        String message = "";

        message = updateFaultPayload(faultNodeId, faultCounter, faultOccurrenceTime, faultObjectId, faultReason,
                faultSeverity);

        LOG.debug("Payload after updating values is: {}",message);

        return sendFaultRequest("POST", message) == 200;

    }

    private static String updateFaultPayload(String faultNodeId, String faultCounter, String faultOccurrenceTime,
            String faultObjectId, String faultReason, String faultSeverity) {
        // @formatter:off
        return FAULT_PAYLOAD.replace("@node-id@", faultNodeId)
                .replace("@counter@", faultCounter)
                .replace("@timestamp@", faultOccurrenceTime)
                .replace("@object-id@", faultObjectId)
                .replace("@problem@", faultReason)
                .replace("@severity@", faultSeverity);
        // @formatter:on
    }


    private int sendFaultRequest(String method, String message) {
        LOG.debug("In sendFaultRequest - {}-{}",method,message);
        BaseHTTPResponse response;
        try {
            String uri = FAULT_NOTIFICATION_URI;
            response = this.sendRequest(uri, method, message, headerMap);
            LOG.debug("finished with responsecode {}", response.code);
            return response.code;
        } catch (IOException e) {
            LOG.warn("problem sending fault message {}", e.getMessage());
            return -1;
        }
    }
}

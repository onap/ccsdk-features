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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.fault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient;

import static org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient.MessageType.*;
import static org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient.SendMethod.*;


public class FaultNotificationClient extends MessageClient {

    private static final String FAULT_NOTIFICATION_URI = "rests/operations/devicemanager:push-fault-notification";
    public static final String NODE_ID = "@node-id@", COUNTER = "@counter@", TIMESTAMP = "@timestamp@",
            OBJECT_ID = "@object-id@", PROBLEM = "@problem@", SEVERITY = "@severity@";
    public static final List<String> REQUIRED_FIELDS = List.of(NODE_ID, COUNTER, TIMESTAMP, OBJECT_ID, PROBLEM, SEVERITY);

    private static final String FAULT_PAYLOAD = "{\n"
            + "  \"input\": {\n"
            + "    \"node-id\": \"" + NODE_ID + "\",\n"
            + "    \"counter\": \"" + COUNTER + "\",\n"
            + "    \"timestamp\": \"" + TIMESTAMP + "\",\n"
            + "    \"object-id\": \"" + OBJECT_ID + "\",\n"
            + "    \"problem\": \"" + PROBLEM + "\",\n"
            + "    \"severity\": \"" + SEVERITY + "\"\n"
            + "  }\n"
            + "}";


    public FaultNotificationClient(String baseUrl) {
        super(baseUrl, FAULT_NOTIFICATION_URI);
    }

    @Override
    public String prepareMessageFromPayloadMap(Map<String, String> notificationPayloadMap) {
        return super.prepareMessageFromPayloadMap(notificationPayloadMap, FAULT_PAYLOAD, REQUIRED_FIELDS);
    }

    @Override
    public boolean sendNotification(String message) {
        return super.sendNotification(message, POST, json);
    }

    public static Map<String, String> createFaultNotificationPayloadMap(String nodeId, String counter, String timestamp,
                                                                        String objectId, String problem, String severity) {
        HashMap<String, String> map = new HashMap<>();
        map.put(NODE_ID, nodeId);
        map.put(COUNTER, counter);
        map.put(TIMESTAMP, timestamp);
        map.put(OBJECT_ID, objectId);
        map.put(PROBLEM, problem);
        map.put(SEVERITY, severity);
        return map;
    }

}

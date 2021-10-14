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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import static org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient.SendMethod.POST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMNotificationClient extends MessageClient {

    private static final String CM_NOTIFICATION_URI = "restconf/operations/devicemanager:push-cm-notification";
    public static final String NODE_ID = "@node-id@", COUNTER = "@counter@", TIMESTAMP = "@timestamp@",
        OBJECT_ID = "@object-id@", NOTIFICATION_TYPE = "@notification-type@", SOURCE_INDICATOR = "@source-indicator@",
        NOTIFICATION_ID = "@notification-id@", PATH = "@path@", OPERATION = "@operation@", VALUE = "@value@";
    public static final List<String> REQUIRED_FIELDS =
        List.of(NODE_ID, COUNTER, TIMESTAMP, OBJECT_ID, NOTIFICATION_TYPE, NOTIFICATION_ID, SOURCE_INDICATOR, PATH,
            OPERATION, VALUE);

    private static final String CM_PAYLOAD = "{\n"
        + "  \"devicemanager:input\": {\n"
        + "    \"devicemanager:node-id\": \"" + NODE_ID + "\",\n"
        + "    \"devicemanager:counter\": \"" + COUNTER + "\",\n"
        + "    \"devicemanager:timestamp\": \"" + TIMESTAMP + "\",\n"
        + "    \"devicemanager:object-id\": \"" + OBJECT_ID + "\",\n"
        + "    \"devicemanager:notification-type\": \"" + NOTIFICATION_TYPE + "\",\n"
        + "    \"devicemanager:notification-id\": \"" + NOTIFICATION_ID + "\",\n"
        + "    \"devicemanager:source-indicator\": \"" + SOURCE_INDICATOR + "\",\n"
        + "    \"devicemanager:path\": \"" + PATH + "\",\n"
        + "    \"devicemanager:operation\": \"" + OPERATION + "\",\n"
        + "    \"devicemanager:value\": \"" + VALUE + "\"\n"
        + "  }\n"
        + "}";

    public CMNotificationClient(String baseUrl) {
        super(baseUrl, CM_NOTIFICATION_URI);
    }

    @Override
    public String prepareMessageFromPayloadMap(Map<String, String> notificationPayloadMap) {
        return super.prepareMessageFromPayloadMap(notificationPayloadMap, CM_PAYLOAD, REQUIRED_FIELDS);
    }

    @Override
    public boolean sendNotification(String message) {
        return super.sendNotification(message, POST, MessageType.json);
    }


    public static Map<String, String> createCMNotificationPayloadMap(String nodeId, String counter, String timestamp,
                                                                     String objectId, String notificationType,
                                                                     String notificationId, String sourceIndicator,
                                                                     String path, String operation,
                                                                     String value) {
        HashMap<String, String> map = new HashMap<>();
        map.put(NODE_ID, nodeId);
        map.put(COUNTER, counter);
        map.put(TIMESTAMP, timestamp);
        map.put(OBJECT_ID, objectId);
        map.put(NOTIFICATION_TYPE, notificationType);
        map.put(NOTIFICATION_ID, notificationId);
        map.put(SOURCE_INDICATOR, sourceIndicator);
        map.put(PATH, path);
        map.put(OPERATION, operation);
        map.put(VALUE, value);
        return map;
    }
}

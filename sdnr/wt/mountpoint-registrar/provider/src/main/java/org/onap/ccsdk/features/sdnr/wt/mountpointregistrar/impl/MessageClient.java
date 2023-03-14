/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2021 Samsung Electronics
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 * ============LICENSE_END==========================================================================
 */

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageClient extends BaseHTTPClient {

    private static final Logger LOG = LoggerFactory.getLogger(MessageClient.class);
    protected final Map<String, String> headerMap;
    private String notificationUri;

	public /* protected */ enum SendMethod {
        PUT, POST
    }

	public /* protected */ enum MessageType {
        xml, json
    }

    public MessageClient(String baseUrl, String notificationUri) {
        super(baseUrl);
        setNotificationUri(notificationUri);
        headerMap = new HashMap<>();
    }

    public void setAuthorization(String username, String password) {
        String credentials = username + ":" + password;
        headerMap.put("Authorization", "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes())));
    }


    public abstract String prepareMessageFromPayloadMap(Map<String, String> notificationPayloadMapMessage);

    protected String prepareMessageFromPayloadMap(Map<String, String> payloadMapMessage, String messagePayload,
                                                  List<String> requiredFields) {
        String message = "";
        if (inputMapHasAllRequiredFields(payloadMapMessage, requiredFields)) {
            message = insertValuesToPayload(payloadMapMessage, messagePayload);
        } else {
            LOG.warn("Input map is missing required fields.");
        }
        return message;
    }

    private boolean inputMapHasAllRequiredFields(Map<String, String> mapToValidate, List<String> requiredFields) {
        if (mapToValidate == null || mapToValidate.isEmpty()) {
            return false;
        }
        for (String requiredField : requiredFields) {
            if (!mapToValidate.containsKey(requiredField)) {
                LOG.warn("Missing required field {}", requiredField);
                return false;
            }
        }
        return true;
    }

    private String insertValuesToPayload(Map<String, String> payloadMapMessage, String payload) {
        for (Map.Entry<String, String> entry : payloadMapMessage.entrySet()) {
            payload = payload.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "null");
        }
        return payload;
    }


    public abstract boolean sendNotification(String message);

    protected boolean sendNotification(String message, SendMethod method, MessageType messageType) {
        LOG.debug("In sendRequestNotification - {}-{}", method, redactMessage(message));
        headerMap.put("Content-Type", "application/".concat(messageType.toString()));
        headerMap.put("Accept", "application/".concat(messageType.toString()));
        BaseHTTPResponse response;
        try {
            response = sendRequest(notificationUri, method.toString(), message, headerMap);
        } catch (IOException e) {
            LOG.warn("Problem sending message: {}", e.getMessage());
            return false;
        }
        LOG.debug("Finished with response code {}", response.code);
        return response.isSuccess();
    }

    protected void setNotificationUri(String notificationUri) {
        this.notificationUri = notificationUri;
    }

    private String redactMessage(String message) {
        String REGEX = "";
        if (message.contains("<key-id")) {
            REGEX = "(<key-id.*>)(.*)(<\\/key-id>)";
        } else if (message.contains("<password")) {
            REGEX = "(<password.*>)(.*)(<\\/password>)";
        } else {
            return message;
        }
        Pattern p = Pattern.compile(REGEX, Pattern.MULTILINE);
        Matcher matcher = p.matcher(message);
        return matcher.replaceAll("$1*********$3");
    }

}

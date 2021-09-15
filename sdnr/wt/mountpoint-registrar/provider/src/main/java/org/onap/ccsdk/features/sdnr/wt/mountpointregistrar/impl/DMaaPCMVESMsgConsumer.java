/*
 * Copyright (C) 2021 Samsung Electronics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class DMaaPCMVESMsgConsumer extends DMaaPVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPCMVESMsgConsumer.class);

    protected DMaaPCMVESMsgConsumer(GeneralConfig generalConfig) {
        super(generalConfig);
        LOG.info("DMaaPCMVESMsgConsumer started successfully! Well done.");
    }

    @Override
    public void processMsg(String msg) throws InvalidMessageException {
        LOG.info("I have message: {}", msg);
        JsonNode rootNode = convertMessageToJsonNode(msg);
        try {
            if (!areBasicEventFieldsValid(rootNode) || !areDetailedEventFieldsValid(rootNode)) {
                LOG.warn("Message is invalid");
                return;
            }
            LOG.info("Message passed validation, processing..");
            // TODO extract data and send them further
        } catch (InvalidMessageException e) {
            LOG.warn("Message is invalid because of: " + e.getMessage());
        }


    }

    @Override
    protected boolean areDetailedEventFieldsValid(JsonNode jsonNode) throws InvalidMessageException {
        List<String> stndDefinedFields = List.of("schemaReference", "data");
        List<String> dataFields = List.of("notificationType", "eventTime", "systemDN");
        List<String> moiChangesFields = List.of("operation", "value", "path", "sourceIndicator");
        try {
            JsonNode stndDefinedFieldsNode = jsonNode.get("event").get("stndDefinedFields").requireNonNull();
            stndDefinedFields.forEach(checkIfFieldExistsInNode(stndDefinedFieldsNode));
            JsonNode dataNode = stndDefinedFieldsNode.get("data");
            dataFields.forEach(checkIfFieldExistsInNode(dataNode));
            JsonNode notificationNode;
            if(dataNode.get("notificationType").textValue().equalsIgnoreCase("notifyMOIChanges")) {
                notificationNode = dataNode.get("moiChanges").get(0);
                moiChangesFields.forEach(checkIfFieldExistsInNode(notificationNode));
            }
        } catch (NullPointerException e) {
            LOG.warn("Message is invalid, sending aborted, processing stopped because of {}", e.getMessage());
            throw new InvalidMessageException(e.getMessage());
        }
        return true;
    }
}


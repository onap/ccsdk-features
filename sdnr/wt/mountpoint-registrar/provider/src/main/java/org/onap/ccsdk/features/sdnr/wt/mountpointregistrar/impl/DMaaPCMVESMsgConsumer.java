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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMaaPCMVESMsgConsumer extends DMaaPVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPCMVESMsgConsumer.class);

    protected DMaaPCMVESMsgConsumer(GeneralConfig generalConfig) {
        super(generalConfig);
        LOG.info("DMaaPCMVESMsgConsumer started successfully");
    }

    @Override
    public void processMsg(String msg) throws InvalidMessageException, JsonProcessingException {
        LOG.debug("Processing CM message {}", msg);
        JsonNode rootNode = convertMessageToJsonNode(msg);
        JsonNode dataNode;
        JsonNode notificationNode;
        try {
            dataNode = rootNode.get("event").get("stndDefinedFields").get("data").requireNonNull();
            if(dataNode.get("notificationType").textValue().equalsIgnoreCase("notifyMOIChanges")) {
                notificationNode = dataNode.get("moiChanges");
                LOG.info("Read CM message from DMaaP topic that is moiChanges type");
            }
        } catch (NullPointerException e) {
            LOG.warn("Message is invalid, sending aborted, processing stopped because of {}", e.getMessage());
            throw new InvalidMessageException(e.getMessage());
        }
        // take required data from notificationNode
    }

}

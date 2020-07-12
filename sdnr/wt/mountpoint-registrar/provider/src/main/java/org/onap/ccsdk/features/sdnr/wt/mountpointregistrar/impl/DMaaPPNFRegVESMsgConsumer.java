/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DMaaPPNFRegVESMsgConsumer extends DMaaPVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPPNFRegVESMsgConsumer.class);
    private static final String DEFAULT_PROTOCOL = "SSH";
    private static final String DEFAULT_PORT = "17830";
    private static final String DEFAULT_USERNAME = "netconf";
    private static final String DEFAULT_PASSWORD = "netconf";
    private static final String DEFAULT_SDNRUSER = "admin";
    private static final String DEFAULT_SDNRPASSWD = "admin";

    @Override
    public void processMsg(String msg) {
        LOG.debug("Message from DMaaP topic is - {} ", msg);
        String pnfId;
        String pnfIPv4Address;
        String pnfCommProtocol;
        String pnfCommPort;
        String pnfKeyId = null;
        String pnfUsername;
        String pnfPasswd = null;
        ObjectMapper oMapper = new ObjectMapper();
        JsonNode dmaapMessageRootNode;
        try {
            dmaapMessageRootNode = oMapper.readTree(msg);
            pnfId = dmaapMessageRootNode.at("/event/commonEventHeader/sourceName").textValue();
            pnfIPv4Address = dmaapMessageRootNode.at("/event/pnfRegistrationFields/oamV4IpAddress").textValue();
            pnfCommProtocol =
                    dmaapMessageRootNode.at("/event/pnfRegistrationFields/additionalFields/protocol").textValue();
            pnfCommPort = dmaapMessageRootNode.at("/event/pnfRegistrationFields/additionalFields/oamPort").textValue();
            if (pnfCommProtocol != null) {
                if (pnfCommProtocol.equalsIgnoreCase("TLS")) {
                    // Read username and keyId
                    pnfKeyId =
                            dmaapMessageRootNode.at("/event/pnfRegistrationFields/additionalFields/keyId").textValue();
                    pnfUsername = dmaapMessageRootNode.at("/event/pnfRegistrationFields/additionalFields/username")
                            .textValue();
                } else if (pnfCommProtocol.equalsIgnoreCase("SSH")) {
                    // Read username and password
                    pnfUsername = dmaapMessageRootNode.at("/event/pnfRegistrationFields/additionalFields/username")
                            .textValue();
                    pnfPasswd = dmaapMessageRootNode.at("/event/pnfRegistrationFields/additionalFields/password")
                            .textValue();
                } else {
                    // log warning - Unknown protocol
                    LOG.warn("Only SSH and TLS protocols supported. Protocol specified in VES message is - {}",
                            pnfCommProtocol, ". Defaulting to SSH");
                    pnfCommProtocol = DEFAULT_PROTOCOL;
                    pnfCommPort = DEFAULT_PORT;
                    pnfUsername = DEFAULT_USERNAME;
                    pnfPasswd = DEFAULT_PASSWORD;
                }
            } else {
                LOG.warn("Protocol not specified in VES message, Defaulting to SSH");
                pnfCommProtocol = DEFAULT_PROTOCOL;
                pnfCommPort = DEFAULT_PORT;
                pnfUsername = DEFAULT_USERNAME;
                pnfPasswd = DEFAULT_PASSWORD;
            }

            LOG.debug("PNF Fields - {} : {} : {} : {} : {} : {} : {}", pnfId, pnfIPv4Address, pnfCommProtocol, pnfKeyId,
                    pnfUsername, pnfPasswd, pnfCommPort);

            String baseUrl = getBaseUrl();
            String sdnrUser = getSDNRUser();
            String sdnrPasswd = getSDNRPasswd();

            PNFMountPointClient mountpointClient = getPNFMountPointClient(baseUrl);
            LOG.debug("Setting RESTConf Authorization values - {} : {}", sdnrUser, sdnrPasswd);
            mountpointClient.setAuthorization(sdnrUser, sdnrPasswd);

            mountpointClient.pnfMountPointCreate(pnfId, pnfIPv4Address, pnfCommProtocol, pnfKeyId, pnfUsername,
                    pnfPasswd, pnfCommPort);
        } catch (IOException e) {
            LOG.info("Cannot parse json object, ignoring the received PNF Registration VES Message. Reason: {}",
                    e.getMessage());
        }
    }

    public String getBaseUrl() {
        return GeneralConfig.getBaseUrl();
    }

    public String getSDNRUser() {
        return GeneralConfig.getSDNRUser() != null ? GeneralConfig.getSDNRUser() : DEFAULT_SDNRUSER;
    }

    public String getSDNRPasswd() {
        return GeneralConfig.getSDNRPasswd() != null ? GeneralConfig.getSDNRPasswd() : DEFAULT_SDNRPASSWD;
    }

    public PNFMountPointClient getPNFMountPointClient(String baseUrl) {
        return new PNFMountPointClient(baseUrl);
    }
}

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMaaPPNFRegVESMsgConsumer extends DMaaPVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPPNFRegVESMsgConsumer.class);
    private static final String DEFAULT_PROTOCOL = "SSH";
    private static final String DEFAULT_PORT = "17830";
    private static final String DEFAULT_USERNAME = "netconf";
    private static final String DEFAULT_PASSWORD = "netconf";
    private static final String DEFAULT_SDNRUSER = "admin";
    private static final String DEFAULT_SDNRPASSWD = "admin";

    private final GeneralConfig generalConfig;

    public DMaaPPNFRegVESMsgConsumer(GeneralConfig generalConfig) {
        this.generalConfig = generalConfig;
    }

    @Override
    public void processMsg(String msg) {
        LOG.debug("Message from DMaaP topic is - {} ", msg);
        @Nullable
        String pnfId;
        String pnfIPAddress;
        @Nullable
        String pnfCommProtocol;
        @Nullable
        String pnfCommPort;
        @Nullable
        String pnfKeyId = null;
        @Nullable
        String pnfUsername;
        @Nullable
        String pnfPasswd = null;
        ObjectMapper oMapper = new ObjectMapper();
        JsonNode dmaapMessageRootNode;
        try {
            dmaapMessageRootNode = oMapper.readTree(msg);
            pnfId = dmaapMessageRootNode.at("/event/commonEventHeader/sourceName").textValue();
            pnfIPAddress = getPNFIPAddress(dmaapMessageRootNode);
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

            LOG.debug(
                    "PNF Fields - ID - {} : IP Address - {} : Protocol - {} : TLS Key ID - {} : User - {} : Port - {}",
                    pnfId, pnfIPAddress, pnfCommProtocol, pnfKeyId, pnfUsername, pnfCommPort);

            String baseUrl = getBaseUrl();
            String sdnrUser = getSDNRUser();
            String sdnrPasswd = getSDNRPasswd();

            PNFMountPointClient mountpointClient = getPNFMountPointClient(baseUrl);
            LOG.debug("Setting RESTConf Authorization values - {} : {}", sdnrUser, sdnrPasswd);
            mountpointClient.setAuthorization(sdnrUser, sdnrPasswd);

            if ((null != pnfId) && null != pnfIPAddress && (null != pnfCommProtocol) && (null != pnfUsername)
                    && (null != pnfCommPort)) {
                mountpointClient.pnfMountPointCreate(pnfId, pnfIPAddress, pnfCommProtocol, pnfKeyId, pnfUsername,
                        pnfPasswd, pnfCommPort);
            } else {
                LOG.warn(
                        "One of the mandatory fields has a null value - pnfId = {} : pnfIPAddress = {} : pnfCommProtocol = {} : pnfUsername {} : "
                                + "pnfCommPort {}",
                        pnfId, pnfIPAddress, pnfCommProtocol, pnfUsername, pnfCommPort,
                        "- not invoking mountpoint creation");
            }
        } catch (IOException e) {
            LOG.info("Cannot parse json object, ignoring the received PNF Registration VES Message. Reason: {}",
                    e.getMessage());
        }
    }

    private String getPNFIPAddress(JsonNode dmaapMessageRootNode) {
        String ipAddress = dmaapMessageRootNode.at("/event/pnfRegistrationFields/oamV6IpAddress").textValue();
        if (ipAddress != null && ipAddress != "")
            return ipAddress;

        ipAddress = dmaapMessageRootNode.at("/event/pnfRegistrationFields/oamV4IpAddress").textValue();
        if (ipAddress != null && ipAddress != "")
            return ipAddress;

        return null;
    }

    public String getBaseUrl() {
        return generalConfig.getBaseUrl();
    }

    public String getSDNRUser() {
        return generalConfig.getSDNRUser() != null ? generalConfig.getSDNRUser() : DEFAULT_SDNRUSER;
    }

    public String getSDNRPasswd() {
        return generalConfig.getSDNRPasswd() != null ? generalConfig.getSDNRPasswd() : DEFAULT_SDNRPASSWD;
    }

    private PNFMountPointClient getPNFMountPointClient(String baseUrl) {
        return new PNFMountPointClient(baseUrl);
    }
}

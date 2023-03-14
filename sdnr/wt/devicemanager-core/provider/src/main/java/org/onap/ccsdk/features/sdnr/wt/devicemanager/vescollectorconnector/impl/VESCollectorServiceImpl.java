/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.NotificationProxyParserImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationProxyParser;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorConfigChangeListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESFaultFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESNotificationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.config.VESCollectorCfgImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VESCollectorServiceImpl implements VESCollectorService, IConfigChangedListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(VESCollectorServiceImpl.class);
    private final VESCollectorCfgImpl vesConfig;
    private final ConfigurationFileRepresentation cfg;
    private BaseHTTPClient httpClient;
    private final Map<String, String> headerMap;
    private List<VESCollectorConfigChangeListener> registeredObjects;
    private final ObjectMapper objMapper;


    public VESCollectorServiceImpl(ConfigurationFileRepresentation config) {
        registeredObjects = new ArrayList<VESCollectorConfigChangeListener>();
        this.vesConfig = new VESCollectorCfgImpl(config);
        this.cfg = config;
        this.cfg.registerConfigChangedListener(this);
        this.objMapper = new ObjectMapper();

        httpClient = new BaseHTTPClient(getBaseUrl(), this.vesConfig.isTrustAllCerts());

        this.headerMap = new HashMap<>();
        this.headerMap.put("Content-Type", "application/json");
        this.headerMap.put("Accept", "application/json");

        setAuthorization(getConfig().getUsername(), getConfig().getPassword());
    }

    @Override
    public VESCollectorCfgImpl getConfig() {
        return this.vesConfig;
    }

    public String getBaseUrl() {
        LOG.debug("IP Address is - {}", getConfig().getIP());
        if (!getConfig().getTLSEnabled()) {
            return "http://" + getConfig().getIP() + ":" + getConfig().getPort();
        } else {
            return "https://" + getConfig().getIP() + ":" + getConfig().getPort();
        }
    }

    private void setAuthorization(String username, String password) {
        if (getConfig().getTLSEnabled()) {
            String credentials = username + ":" + password;
            this.headerMap.put("Authorization",
                    "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes())));
        }

    }

    @Override
    public boolean publishVESMessage(VESMessage message) {
        LOG.debug("In VESClient - {} ", message.getMessage());
        BaseHTTPResponse response;
        try {
            String uri = "eventListener" + "/" + getConfig().getVersion();
            response = httpClient.sendRequest(uri, "POST", message.getMessage(), headerMap);
            LOG.debug("finished with responsecode {}", response.code);
            return response.code == 200;
        } catch (IOException e) {
            LOG.warn("problem publishing VES message {} ", e.getMessage());
            return false;
        }

    }

    @Override
    public void close() throws Exception {
        this.cfg.unregisterConfigChangedListener(this);
    }

    @Override
    public void onConfigChanged() {
        LOG.debug("In onConfigChanged - isTrustAllCerts = {} getBaseUrl = {}", getConfig().isTrustAllCerts(), getBaseUrl());
        httpClient = new BaseHTTPClient(getBaseUrl(), this.vesConfig.isTrustAllCerts());
        setAuthorization(getConfig().getUsername(), getConfig().getPassword());
        Iterator<VESCollectorConfigChangeListener> it = registeredObjects.iterator();
        while (it.hasNext()) {
            VESCollectorConfigChangeListener o = it.next();
            o.notify(getConfig());
        }
    }

    @Override
    public void registerForChanges(VESCollectorConfigChangeListener o) {
        registeredObjects.add(o);
    }

    @Override
    public void deregister(VESCollectorConfigChangeListener o) {
        registeredObjects.remove(o);
    }

    @Override
    public @NonNull NotificationProxyParser getNotificationProxyParser() {
        return new NotificationProxyParserImpl();
    }

    /**
     * Generates VES Event JSON containing commonEventHeader and notificationFields fields
     *
     * @param commonEventHeader
     * @param notifFields
     * @return VESMessage - representing the VESEvent JSON
     * @throws JsonProcessingException
     */
    @Override
    public VESMessage generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader,
            VESNotificationFieldsPOJO notifFields) throws JsonProcessingException {
        Map<String, Object> innerEvent = new HashMap<String, Object>();
        innerEvent.put("commonEventHeader", commonEventHeader);
        innerEvent.put("notificationFields", notifFields);

        Map<String, Object> outerEvent = new HashMap<String, Object>();
        outerEvent.put("event", innerEvent);
        LOG.debug("In generateVESEvent - {}", objMapper.writeValueAsString(outerEvent));
        return new VESMessage(objMapper.writeValueAsString(outerEvent));
    }

    /**
     * Generates VES Event JSON containing commonEventHeader and faultFields fields
     *
     * @param commonEventHeader
     * @param faultFields
     * @return VESMessage - representing the VES Event JSON
     * @throws JsonProcessingException
     */
    @Override
    public VESMessage generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESFaultFieldsPOJO faultFields) throws JsonProcessingException {
        Map<String, Object> innerEvent = new HashMap<String, Object>();
        innerEvent.put("commonEventHeader", commonEventHeader);
        innerEvent.put("faultFields", faultFields);

        Map<String, Object> outerEvent = new HashMap<String, Object>();
        outerEvent.put("event", innerEvent);
        return new VESMessage(objMapper.writeValueAsString(outerEvent));
    }

    /**
     * Generates VES Event JSON containing commonEventHeader and pnfRegistration fields
     *
     * @param commonEventHeader
     * @param pnfRegistrationFields
     * @return VESMessage - representing the VES Event JSON
     * @throws JsonProcessingException
     */
    @Override
    public VESMessage generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESPNFRegistrationFieldsPOJO pnfRegistrationFields) throws JsonProcessingException {
        Map<String, Object> innerEvent = new HashMap<String, Object>();
        innerEvent.put("commonEventHeader", commonEventHeader);
        innerEvent.put("pnfRegistrationFields", pnfRegistrationFields);

        Map<String, Object> outerEvent = new HashMap<String, Object>();
        outerEvent.put("event", innerEvent);
        return new VESMessage(objMapper.writeValueAsString(outerEvent));
    }

}

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorConfigChangeListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.config.VESCollectorCfgImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VESCollectorClient implements VESCollectorService, IConfigChangedListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(VESCollectorClient.class);
    private final VESCollectorCfgImpl vesConfig;
    private final ConfigurationFileRepresentation cfg;
    private BaseHTTPClient httpClient;
    private final Map<String, String> headerMap;
    private List<VESCollectorConfigChangeListener> registeredObjects;

    public VESCollectorClient(ConfigurationFileRepresentation config) {
        registeredObjects = new ArrayList<VESCollectorConfigChangeListener>();
        this.vesConfig = new VESCollectorCfgImpl(config);
        this.cfg = config;
        this.cfg.registerConfigChangedListener(this);

        httpClient = new BaseHTTPClient(getBaseUrl());

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
    public boolean publishVESMessage(String message) {
        LOG.info("In VESClient - {} ", message);
        BaseHTTPResponse response;
        try {
            String uri = "eventListener" + "/" + getConfig().getVersion();
            response = httpClient.sendRequest(uri, "POST", message, headerMap);
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
        httpClient.setBaseUrl(getBaseUrl());
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

}

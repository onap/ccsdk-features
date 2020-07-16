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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl.config.AaiConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.InventoryProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.AaiService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InventoryInformationDcae;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiProviderClient implements AaiService, AutoCloseable {

    private static Logger LOG = LoggerFactory.getLogger(AaiProviderClient.class);
    @SuppressWarnings("unused") // @TODO Remove code
    private static boolean reloadConfigFlag;
    private static final IConfigChangedListener configChangedListener = () -> reloadConfigFlag = true;

    private final AaiConfig config;
    private final DeviceManagerImpl deviceManager;
    private final ConfigurationFileRepresentation htconfig;


    public AaiProviderClient(@Nonnull ConfigurationFileRepresentation cfg, DeviceManagerImpl devMgr) {
        HtAssert.nonnull(cfg);
        this.config = new AaiConfig(cfg);
        LOG.debug("AaiProviderClient configuration setting: {}", this.config);
        this.htconfig = cfg;
        this.htconfig.registerConfigChangedListener(configChangedListener);
        this.deviceManager = devMgr;

    }

    public AaiConfig getConfig() {
        return this.config;
    }

    public void onDeviceRegistered(String mountPointName) {
        if (this.config.isOff()) {
            return;
        }
        NetworkElement ne =
                this.deviceManager != null ? this.deviceManager.getConnectedNeByMountpoint(mountPointName) : null;
        Optional<InventoryProvider> oip = ne != null ? ne.getService(InventoryProvider.class) : Optional.empty();
        this.onDeviceRegistered(mountPointName,
                oip.isPresent() ? oip.get().getInventoryInformation("MWPS") : InventoryInformationDcae.getDefault());
    }

    public void onDeviceRegistered(String mountPointName, InventoryInformationDcae i) {
        if (this.config.isOff()) {
            return;
        }
        new Thread(new AaiCreateRequestRunnable(mountPointName, i.getType(), i.getModel(), i.getVendor(),
                i.getDeviceIpv4(), i.getInterfaceUuidList())).start();
    }

    public void onDeviceUnregistered(String mountPointName) {
        if (this.config.isOff()) {
            return;
        }
        if (this.config.doDeleteOnMountPointRemoved()) {
            new Thread(new AaiDeleteRequestRunnable(mountPointName)).start();
        } else {
            LOG.debug("prevent deleting device {} by config", mountPointName);
        }
    }

    @Override
    public void close() throws Exception {
        this.htconfig.unregisterConfigChangedListener(configChangedListener);
    }

    private class AaiCreateRequestRunnable implements Runnable {

        private static final int RESPCODE_NOTFOUND = BaseHTTPResponse.CODE404;
        private static final int RESPCODE_FOUND = BaseHTTPResponse.CODE200;
        private final AaiWebApiClient mClient;
        private final String pnfId;
        private final String type;
        private final String model;
        private final String vendor;
        private final String oamIp;
        private final List<String> ifaces;
        private final int timeout;

        public AaiCreateRequestRunnable(String pnfId, String type, String model, String vendor, String oamIp,
                List<String> ifaces) {
            this.pnfId = pnfId;
            this.type = type;
            this.model = model;
            this.vendor = vendor;
            this.oamIp = oamIp;
            this.ifaces = ifaces;
            this.timeout = AaiProviderClient.this.config.getConnectionTimeout();
            this.mClient = new AaiWebApiClient(AaiProviderClient.this.config.getBaseUrl(),
                    AaiProviderClient.this.config.getHeaders(), AaiProviderClient.this.config.getTrustAll(),
                    AaiProviderClient.this.config.getPcks12CertificateFilename(),
                    AaiProviderClient.this.config.getPcks12CertificatePassphrase());
        }

        @Override
        public void run() {
            LOG.debug("check if pnfid {} exists", pnfId);
            this.mClient.setTimeout(timeout);
            int responseCode = this.mClient.pnfCheckIfExists(pnfId);
            if (responseCode == RESPCODE_NOTFOUND) {
                LOG.debug("do pnfCreate for {}", pnfId);
                this.mClient.pnfCreate(pnfId, type, model, vendor, oamIp, ifaces);
            } else if (responseCode == RESPCODE_FOUND) {
                LOG.debug("pnfid {} found, nothing to do", pnfId);
            } else {
                LOG.warn("unhandled response code: {}", responseCode);
            }
        }
    };

    private class AaiDeleteRequestRunnable implements Runnable {

        private final AaiWebApiClient mClient;
        private final String pnfId;
        private final int timeout;


        public AaiDeleteRequestRunnable(String pnfId) {
            this.pnfId = pnfId;
            this.timeout = AaiProviderClient.this.config.getConnectionTimeout();
            this.mClient = new AaiWebApiClient(AaiProviderClient.this.config.getBaseUrl(),
                    AaiProviderClient.this.config.getHeaders(), AaiProviderClient.this.config.getTrustAll(),
                    AaiProviderClient.this.config.getPcks12CertificateFilename(),
                    AaiProviderClient.this.config.getPcks12CertificatePassphrase());
        }

        @Override
        public void run() {
            this.mClient.setTimeout(this.timeout);
            this.mClient.pnfDelete(pnfId);
        }
    };

}

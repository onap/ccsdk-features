/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl;

import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.InventoryInformation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.ONFCoreNetworkElementRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.HtDevicemanagerConfiguration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.AaiConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiProviderClient implements AutoCloseable {

    private static Logger LOG = LoggerFactory.getLogger(AaiProviderClient.class);
    private static boolean reloadConfigFlag;
    private static final IConfigChangedListener configChangedListener = () -> reloadConfigFlag = true;

    private AaiConfig config;
    private final DeviceManagerImpl deviceManager;
    private final HtDevicemanagerConfiguration htconfig;


    public AaiConfig getConfig() {
        return this.config;
    }


    public AaiProviderClient(HtDevicemanagerConfiguration cfg, DeviceManagerImpl devMgr) {
        this.config = cfg.getAai();
        this.htconfig = cfg;
        this.htconfig.registerConfigChangedListener(configChangedListener);
        this.deviceManager = devMgr;

    }

    private void _reload() {
        if (reloadConfigFlag) {
            this.config = AaiConfig.reload();
            LOG.info("config reloaded: {}", config == null ? "null" : config);
        }
        reloadConfigFlag = false;
    }

    public void onDeviceRegistered(String mountPointName) {
        this._reload();
        if (this.config.isOff()) {
            return;
        }
        ONFCoreNetworkElementRepresentation ne =
                this.deviceManager != null ? this.deviceManager.getNeByMountpoint(mountPointName) : null;
        this.onDeviceRegistered(mountPointName,
                ne != null ? ne.getInventoryInformation("MWPS") : InventoryInformation.getDefault());

    }

    public void onDeviceRegistered(String mountPointName, InventoryInformation i) {
        this._reload();
        if (this.config.isOff()) {
            return;
        }
        new Thread(new AaiCreateRequestRunnable(mountPointName, i.getType(), i.getModel(), i.getVendor(),
                i.getDeviceIpv4(), i.getInterfaceUuidList())).start();
    }

    public void onDeviceUnregistered(String mountPointName) {
        this._reload();
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

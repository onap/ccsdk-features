/*
 * ============LICENSE_START==========================================
 * Copyright (c) 2019 PANTHEON.tech s.r.o.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.features.sdnr.wt.apigateway.lighty.SdnrWtApigatewayModule;
import org.onap.ccsdk.features.sdnr.wt.apigateway.lighty.SdnrWtDeviceManagerModule;
import org.onap.ccsdk.features.sdnr.wt.helpserver.lighty.SdnrWtHelpServerModule;
import org.onap.ccsdk.features.sdnr.wt.odlux.lighty.SdnrWtOdluxModule;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager2.lighty.SdnrWtWebsocketManager2Module;
import org.onap.ccsdk.sli.core.lighty.common.CcsdkLightyUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that groups all other LightyModules
 * from the sdnr-wt artifact so they can be all treated as one component (for example started/stopped at once).
 * For more information about the lighty.io visit the website https://lighty.io.
 */
public class SdnrWtModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SdnrWtModule.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final NotificationPublishService notificationPublishService;
    private final MountPointService mountPointService;
    private final ClusterSingletonServiceProvider clusteringSingletonService;

    private SdnrWtApigatewayModule sdnrWtApigatewayModule;
    private SdnrWtDeviceManagerModule sdnrWtDeviceManagerModule;
    private SdnrWtHelpServerModule sdnrWtHelpServerModule;
    private SdnrWtOdluxModule sdnrWtOdluxModule;
    private SdnrWtWebsocketManager2Module sdnrWtWebsocketManager2Module;

    public SdnrWtModule(DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry,
            NotificationPublishService notificationPublishService, MountPointService mountPointService,
            ClusterSingletonServiceProvider clusteringSingletonService) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationPublishService = notificationPublishService;
        this.mountPointService = mountPointService;
        this.clusteringSingletonService = clusteringSingletonService;
    }

    @Override
    protected boolean initProcedure() {
        LOG.debug("Initializing SDNR WT Lighty module...");

        this.sdnrWtApigatewayModule = new SdnrWtApigatewayModule();
        if (!CcsdkLightyUtils.startLightyModule(sdnrWtApigatewayModule)) {
            return false;
        }

        this.sdnrWtDeviceManagerModule = new SdnrWtDeviceManagerModule(dataBroker, rpcProviderRegistry,
                notificationPublishService, mountPointService, clusteringSingletonService);
        if (!CcsdkLightyUtils.startLightyModule(sdnrWtDeviceManagerModule)) {
            return false;
        }

        this.sdnrWtHelpServerModule = new SdnrWtHelpServerModule();
        if (!CcsdkLightyUtils.startLightyModule(sdnrWtHelpServerModule)) {
            return false;
        }

        this.sdnrWtOdluxModule = new SdnrWtOdluxModule();
        if (!CcsdkLightyUtils.startLightyModule(sdnrWtOdluxModule)) {
            return false;
        }

        this.sdnrWtWebsocketManager2Module = new SdnrWtWebsocketManager2Module();
        if (!CcsdkLightyUtils.startLightyModule(sdnrWtWebsocketManager2Module)) {
            return false;
        }

        LOG.debug("SDNR WT Lighty module was initialized successfully");
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        LOG.debug("Stopping SDNR WT Lighty module...");

        boolean stopSuccessful = true;

        if (!CcsdkLightyUtils.stopLightyModule(sdnrWtWebsocketManager2Module)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(sdnrWtOdluxModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(sdnrWtHelpServerModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(sdnrWtDeviceManagerModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(sdnrWtApigatewayModule)) {
            stopSuccessful = false;
        }

        if (stopSuccessful) {
            LOG.debug("SDNR WT Lighty module was stopped successfully");
        } else {
            LOG.error("SDNR WT Lighty module was not stopped successfully!");
        }
        return stopSuccessful;
    }

    public SdnrWtApigatewayModule getSdnrWtApigatewayModule() {
        return sdnrWtApigatewayModule;
    }

    public SdnrWtDeviceManagerModule getSdnrWtDeviceManagerModule() {
        return sdnrWtDeviceManagerModule;
    }

    public SdnrWtHelpServerModule getSdnrWtHelpServerModule() {
        return sdnrWtHelpServerModule;
    }

    public SdnrWtOdluxModule getSdnrWtOdluxModule() {
        return sdnrWtOdluxModule;
    }

    public SdnrWtWebsocketManager2Module getSdnrWtWebsocketManager2Module() {
        return sdnrWtWebsocketManager2Module;
    }
}

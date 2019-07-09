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
package org.onap.ccsdk.features.sdnr.wt.apigateway.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the sdnr-wt-devicemanager-provider artifact.
 */
public class SdnrWtDeviceManagerModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SdnrWtDeviceManagerModule.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final NotificationPublishService notificationPublishService;
    private final MountPointService mountPointService;
    private final ClusterSingletonServiceProvider clusteringSingletonService;

    private DeviceManagerImpl deviceManager;

    public SdnrWtDeviceManagerModule(DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry,
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
        deviceManager = new DeviceManagerImpl();
        deviceManager.setDataBroker(dataBroker);
        deviceManager.setRpcProviderRegistry(rpcProviderRegistry);
        deviceManager.setNotificationPublishService(notificationPublishService);
        deviceManager.setMountPointService(mountPointService);
        deviceManager.setClusterSingletonService(clusteringSingletonService);
        deviceManager.setResources(new ResourcesLighty());
        deviceManager.init();
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        try {
            deviceManager.close();
        } catch (Exception e) {
            LOG.error("Unable to stop device manager ({})!", deviceManager.getClass(), e);
            return false;
        }
        return true;
    }

}

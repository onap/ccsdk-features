/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Update Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping.ResyncNetworkElementsListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.MaintenanceRPCServiceAPI;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl.MaintenanceServiceImpl;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodenameInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodenameOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodenameOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.DevicemanagerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushAttributeChangeNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushAttributeChangeNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushCmNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushCmNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeOutputBuilder;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerApiServiceImpl implements DevicemanagerService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DevicemanagerService.class);

    private final ObjectRegistration<DevicemanagerService> rpcReg;
    private @Nullable final MaintenanceRPCServiceAPI maintenanceService;
    private @Nullable final PushNotifications pushNotificationsListener;
    private @Nullable final ResyncNetworkElementsListener resyncCallbackListener;

    public DeviceManagerApiServiceImpl(final RpcProviderService rpcProviderRegistry,
            MaintenanceServiceImpl maintenanceService, ResyncNetworkElementsListener listener,
            PushNotifications pushNotificationsListener) {
        this.maintenanceService = maintenanceService;
        this.pushNotificationsListener = pushNotificationsListener;
        this.resyncCallbackListener = listener;

        // Register ourselves as the REST API RPC implementation
        LOG.info("Register RPC Service " + DevicemanagerService.class.getSimpleName());
        this.rpcReg = rpcProviderRegistry.registerRpcImplementation(DevicemanagerService.class, this);
    }

    @Override
    public void close() throws Exception {
        LOG.info("Close RPC Service");
        if (rpcReg != null) {
            rpcReg.close();
        }
    }

    /*-------------------------------
     * Interfaces for MaintenanceService
     */

    @Override
    public ListenableFuture<RpcResult<GetRequiredNetworkElementKeysOutput>> getRequiredNetworkElementKeys(
            GetRequiredNetworkElementKeysInput input) {
        return getRequiredNetworkElementKeys();
    }

    // For casablanca version no input was generated.
    public ListenableFuture<RpcResult<GetRequiredNetworkElementKeysOutput>> getRequiredNetworkElementKeys() {

        LOG.debug("RPC Request: getRequiredNetworkElementKeys");
        RpcResultBuilder<GetRequiredNetworkElementKeysOutput> result;
        try {
            GetRequiredNetworkElementKeysOutputBuilder outputBuilder =
                    maintenanceService.getRequiredNetworkElementKeys();
            result = RpcResultBuilder.success(outputBuilder.build());
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ShowRequiredNetworkElementOutput>> showRequiredNetworkElement(
            ShowRequiredNetworkElementInput input) {

        LOG.debug("RPC Request: showRequiredNetworkElement input: {}", input.getMountpointName());
        RpcResultBuilder<ShowRequiredNetworkElementOutput> result;

        try {
            ShowRequiredNetworkElementOutputBuilder outputBuilder =
                    maintenanceService.showRequiredNetworkElement(input);
            result = RpcResultBuilder.success(outputBuilder.build());
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<SetMaintenanceModeOutput>> setMaintenanceMode(SetMaintenanceModeInput input) {

        LOG.debug("RPC Request: setMaintenanceMode input: {}", input.getNodeId());
        RpcResultBuilder<SetMaintenanceModeOutput> result;

        try {
            SetMaintenanceModeOutputBuilder outputBuilder = maintenanceService.setMaintenanceMode(input);
            result = RpcResultBuilder.success(outputBuilder.build());
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }



    @Override
    public ListenableFuture<RpcResult<GetMaintenanceModeOutput>> getMaintenanceMode(GetMaintenanceModeInput input) {

        LOG.debug("RPC Request: getMaintenanceMode input: {}", input.getMountpointName());
        @NonNull RpcResultBuilder<GetMaintenanceModeOutput> result;

        try {
            GetMaintenanceModeOutputBuilder outputBuilder = maintenanceService.getMaintenanceMode(input);
            result = RpcResultBuilder.success(outputBuilder.build());
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();

    }

    @Override
    public ListenableFuture<RpcResult<TestMaintenanceModeOutput>> testMaintenanceMode(TestMaintenanceModeInput input) {
        LOG.debug("RPC Request: getMaintenanceMode input: {}", input.getMountpointName());
        RpcResultBuilder<TestMaintenanceModeOutput> result;

        try {
            TestMaintenanceModeOutputBuilder outputBuilder = maintenanceService.testMaintenanceMode(input);
            result = RpcResultBuilder.success(outputBuilder.build());
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();

    }


    @Override
    public ListenableFuture<RpcResult<ClearCurrentFaultByNodenameOutput>> clearCurrentFaultByNodename(
            ClearCurrentFaultByNodenameInput input) {
        LOG.debug("RPC Request: clearNetworkElementAlarms input: {}", input.getNodenames());
        RpcResultBuilder<ClearCurrentFaultByNodenameOutput> result;
        try {
            if (this.resyncCallbackListener != null) {
                List<String> nodeNames =
                        this.resyncCallbackListener.doClearCurrentFaultByNodename(input.getNodenames().stream().collect(Collectors.toList()));
                ClearCurrentFaultByNodenameOutputBuilder outputBuilder = new ClearCurrentFaultByNodenameOutputBuilder();
                outputBuilder.setNodenames(new HashSet<>(nodeNames));
                result = RpcResultBuilder.success(outputBuilder.build());
            } else {
                result = RpcResultBuilder.failed();
                result.withError(ErrorType.APPLICATION, "Startup running");
            }
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<PushFaultNotificationOutput>> pushFaultNotification(
            PushFaultNotificationInput input) {
        LOG.debug("RPC Received fault notification {}", input);
        RpcResultBuilder<PushFaultNotificationOutput> result;
        try {
            pushNotificationsListener.pushFaultNotification(input);
            result = RpcResultBuilder.success();
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<PushCmNotificationOutput>> pushCmNotification(PushCmNotificationInput input) {
        LOG.debug("RPC Received CM notification {}", input);
        RpcResultBuilder<PushCmNotificationOutput> result;
        try {
            pushNotificationsListener.pushCMNotification(input);
            result = RpcResultBuilder.success();
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<PushAttributeChangeNotificationOutput>> pushAttributeChangeNotification(
            PushAttributeChangeNotificationInput input) {
        LOG.debug("RPC Received change notification {}", input);
        RpcResultBuilder<PushAttributeChangeNotificationOutput> result;
        try {
            pushNotificationsListener.pushAttributeChangeNotification(input);
            result = RpcResultBuilder.success();
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

}

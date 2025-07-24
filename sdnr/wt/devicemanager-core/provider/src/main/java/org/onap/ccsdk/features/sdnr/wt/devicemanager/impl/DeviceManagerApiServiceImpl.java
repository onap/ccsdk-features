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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodename;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodenameInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodenameOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodenameOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushAttributeChangeNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushAttributeChangeNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushAttributeChangeNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushCmNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushCmNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushCmNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElement;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeOutputBuilder;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerApiServiceImpl implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerApiServiceImpl.class);

    private final Registration rpcReg;
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
        LOG.info("Register RPC Service DevicemanagerService");
        this.rpcReg = rpcProviderRegistry.registerRpcImplementations(List.of(
                new Rpc<GetRequiredNetworkElementKeysInput, GetRequiredNetworkElementKeysOutput>() {
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull GetRequiredNetworkElementKeysOutput>> invoke(
                            @NonNull GetRequiredNetworkElementKeysInput input) {
                        return DeviceManagerApiServiceImpl.this.getRequiredNetworkElementKeys(input);
                    }
                    @Override
                    public @NonNull Class<? extends Rpc<GetRequiredNetworkElementKeysInput, GetRequiredNetworkElementKeysOutput>> implementedInterface() {
                        return GetRequiredNetworkElementKeys.class;
                    }
                },
                new Rpc<ShowRequiredNetworkElementInput, ShowRequiredNetworkElementOutput>(){
                    @Override
                    public @NonNull Class<? extends Rpc<ShowRequiredNetworkElementInput, ShowRequiredNetworkElementOutput>> implementedInterface() {
                        return ShowRequiredNetworkElement.class;
                    }
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull ShowRequiredNetworkElementOutput>> invoke(
                            @NonNull ShowRequiredNetworkElementInput input) {
                        return DeviceManagerApiServiceImpl.this.showRequiredNetworkElement(input);
                    }
                },
                new Rpc<SetMaintenanceModeInput,SetMaintenanceModeOutput>(){
                    @Override
                    public @NonNull Class<? extends Rpc<SetMaintenanceModeInput, SetMaintenanceModeOutput>> implementedInterface() {
                        return SetMaintenanceMode.class;
                    }
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull SetMaintenanceModeOutput>> invoke(
                            @NonNull SetMaintenanceModeInput input) {
                        return DeviceManagerApiServiceImpl.this.setMaintenanceMode(input);
                    }
                },
                new Rpc<GetMaintenanceModeInput,GetMaintenanceModeOutput>(){
                    @Override
                    public @NonNull Class<? extends Rpc<GetMaintenanceModeInput, GetMaintenanceModeOutput>> implementedInterface() {
                        return GetMaintenanceMode.class;
                    }
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull GetMaintenanceModeOutput>> invoke(
                            @NonNull GetMaintenanceModeInput input) {
                        return DeviceManagerApiServiceImpl.this.getMaintenanceMode(input);
                    }
                },
                new Rpc<TestMaintenanceModeInput,TestMaintenanceModeOutput>(){
                    @Override
                    public @NonNull Class<? extends Rpc<TestMaintenanceModeInput, TestMaintenanceModeOutput>> implementedInterface() {
                        return TestMaintenanceMode.class;
                    }
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull TestMaintenanceModeOutput>> invoke(
                            @NonNull TestMaintenanceModeInput input) {
                        return DeviceManagerApiServiceImpl.this.testMaintenanceMode(input);
                    }
                },
                new Rpc<ClearCurrentFaultByNodenameInput,ClearCurrentFaultByNodenameOutput>(){
                    @Override
                    public @NonNull Class<? extends Rpc<ClearCurrentFaultByNodenameInput, ClearCurrentFaultByNodenameOutput>> implementedInterface() {
                        return ClearCurrentFaultByNodename.class;
                    }
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull ClearCurrentFaultByNodenameOutput>> invoke(
                            @NonNull ClearCurrentFaultByNodenameInput input) {
                        return DeviceManagerApiServiceImpl.this.clearCurrentFaultByNodename(input);
                    }
                },
                new Rpc<PushFaultNotificationInput,PushFaultNotificationOutput>(){
                    @Override
                    public @NonNull Class<? extends Rpc<PushFaultNotificationInput, PushFaultNotificationOutput>> implementedInterface() {
                        return PushFaultNotification.class;
                    }
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull PushFaultNotificationOutput>> invoke(
                            @NonNull PushFaultNotificationInput input) {
                        return DeviceManagerApiServiceImpl.this.pushFaultNotification(input);
                    }
                },
                new Rpc<PushCmNotificationInput,PushCmNotificationOutput>(){
                    @Override
                    public @NonNull Class<? extends Rpc<PushCmNotificationInput, PushCmNotificationOutput>> implementedInterface() {
                        return PushCmNotification.class;
                    }
                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull PushCmNotificationOutput>> invoke(
                            @NonNull PushCmNotificationInput input) {
                        return DeviceManagerApiServiceImpl.this.pushCmNotification(input);
                    }
                },
                new Rpc<PushAttributeChangeNotificationInput,PushAttributeChangeNotificationOutput>(){

                    @Override
                    public @NonNull Class<? extends Rpc<PushAttributeChangeNotificationInput, PushAttributeChangeNotificationOutput>> implementedInterface() {
                        return PushAttributeChangeNotification.class;
                    }

                    @Override
                    public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull PushAttributeChangeNotificationOutput>> invoke(
                            @NonNull PushAttributeChangeNotificationInput input) {
                        return DeviceManagerApiServiceImpl.this.pushAttributeChangeNotification(input);
                    }
                }
        ));
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

    public ListenableFuture<RpcResult<ClearCurrentFaultByNodenameOutput>> clearCurrentFaultByNodename(
            ClearCurrentFaultByNodenameInput input) {
        final var nodeNamesInput = input.getNodenames();
        LOG.debug("RPC Request: clearNetworkElementAlarms input: {}", nodeNamesInput);
        RpcResultBuilder<ClearCurrentFaultByNodenameOutput> result;
        if(nodeNamesInput==null){
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "input is null");
            return result.buildFuture();
        }
        try {
            if (this.resyncCallbackListener != null) {
                List<String> nodeNames =
                        this.resyncCallbackListener.doClearCurrentFaultByNodename(nodeNamesInput.stream().collect(Collectors.toList()));
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

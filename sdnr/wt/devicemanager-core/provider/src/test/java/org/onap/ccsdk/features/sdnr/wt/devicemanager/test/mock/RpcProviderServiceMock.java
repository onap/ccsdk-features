/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ClassToInstanceMap;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerApiServiceImpl;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeys;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysOutput;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.concepts.Registration;


public class RpcProviderServiceMock implements RpcProviderService {

    private DeviceManagerApiServiceImpl deviceManagerApi = mock(DeviceManagerApiServiceImpl.class);


    @Override
    public @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation) {
        return null;
    }

    @Override
    public @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation,
            Set<DataObjectIdentifier<?>> paths) {
        return null;
    }


    @Override
    public @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations) {

        when(deviceManagerApi.getMaintenanceMode(any())).thenAnswer(
                i -> ((Rpc<GetMaintenanceModeInput, GetMaintenanceModeOutput>) implementations.entrySet().stream()
                        .filter(e -> e.getValue().implementedInterface().equals(
                                GetMaintenanceMode.class)).findFirst().get().getValue()).invoke(i.getArgument(0)));
        when(deviceManagerApi.getRequiredNetworkElementKeys(any())).thenAnswer(
                i -> ((Rpc<GetRequiredNetworkElementKeysInput, GetRequiredNetworkElementKeysOutput>) implementations.entrySet()
                        .stream().filter(e -> e.getValue().implementedInterface().equals(
                                GetRequiredNetworkElementKeys.class)).findFirst().get().getValue()).invoke(
                        i.getArgument(0)));
       /* when(deviceManagerApi.getMaintenanceMode(any())).thenReturn(implementations.entrySet().stream().filter(e->e.getValue().implementedInterface().equals(
                GetMaintenanceMode.class)).findFirst().get().getValue().invoke(null));*/

        return new Registration() {
            @Override
            public void close() {

            }
        };
    }

    @Override
    public @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations,
            Set<DataObjectIdentifier<?>> paths) {
        return null;
    }


    public DeviceManagerApiServiceImpl getDeviceManagerApiService() {
        return deviceManagerApi;
    }

}

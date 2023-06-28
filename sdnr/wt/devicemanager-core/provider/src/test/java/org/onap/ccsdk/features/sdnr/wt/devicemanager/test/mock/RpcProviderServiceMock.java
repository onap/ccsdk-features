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

import java.util.Set;

import com.google.common.collect.ClassToInstanceMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.DevicemanagerService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcProviderServiceMock implements RpcProviderService {

    private DevicemanagerService deviceManagerApi;

    @Override
    public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
            T implementation) {
        System.out.println("Register class " + implementation);
        if (implementation instanceof DevicemanagerService) {
            deviceManagerApi = (DevicemanagerService) implementation;
        }
        return null;
    }

    @Override
    public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
            T implementation, Set<InstanceIdentifier<?>> paths) {
        System.out.println("Register class " + implementation);
        if (implementation instanceof DevicemanagerService) {
            deviceManagerApi = (DevicemanagerService) implementation;
        }
        return null;
    }

    @Override
    public @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation) {
        System.out.println("Register class " + implementation);
        if (implementation instanceof DevicemanagerService) {
            deviceManagerApi = (DevicemanagerService) implementation;
        }
        return null;
    }

    @Override
    public @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation, Set<InstanceIdentifier<?>> paths) {
        System.out.println("Register class " + implementation);
        if (implementation instanceof DevicemanagerService) {
            deviceManagerApi = (DevicemanagerService) implementation;
        }
        return null;
    }

    @Override
    public @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations) {
        return null;
    }

    @Override
    public @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations, Set<InstanceIdentifier<?>> paths) {
        return null;
    }

    public DevicemanagerService getDeviceManagerApiService() {
        return deviceManagerApi;
    }

}

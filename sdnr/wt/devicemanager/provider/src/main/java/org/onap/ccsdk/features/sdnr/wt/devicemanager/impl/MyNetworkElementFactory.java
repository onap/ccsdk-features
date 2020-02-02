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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.opendaylight.mdsal.binding.api.MountPoint;

/**
 * @author herbert
 *
 */
class MyNetworkElementFactory<L extends NetworkElementFactory> {


    @FunctionalInterface
    interface Register<X, Y, Z> {
        public void register(X mountPointNodeName, Y mountPoint, Z ne);
    }

    private final Register<String, MountPoint, NetworkElement> init;
    private final @NonNull L factory;

    @SuppressWarnings("null")
    MyNetworkElementFactory(@NonNull L factory, Register<String, MountPoint, NetworkElement> init) {
        super();
        if (init == null || factory == null) {
            throw new IllegalArgumentException("Null not allowed here.");
        }
        this.init = init;
        this.factory = factory;
    }
    public Register<String, MountPoint, NetworkElement> getInit() {
        return init;
    }
    public @NonNull L getFactory() {
        return factory;
    }
}

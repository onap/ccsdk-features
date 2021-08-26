/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2021 Wipro Limited.
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

package org.onap.ccsdk.features.sdnr.northbound.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandleInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandleOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.CMHandleAPIService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCMHandleProvider implements CMHandleAPIService {

    private static final Logger LOG = LoggerFactory.getLogger(AddCMHandleProvider.class);

    private final String appName = "addCMHandle";

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration<CMHandleAPIService> serviceRegistration;

    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final String PROPERTIES_FILE_NAME = "cm-handle.properties";
    private static final String PARSING_ERROR =
            "Could not create the request message to send to the server; no message will be sent";

    public AddCMHandleProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(CMHandleAPIService.class, this);

        LOG.debug("Initializing provider for " + appName);

        Preconditions.checkNotNull(dataBroker, "dataBroker must be set");

        String propDir = System.getenv(SDNC_CONFIG_DIR);
        if (propDir == null) {
            LOG.error("Environment variable SDNC_CONFIG_DIR is not set");
            propDir = "/opt/onap/ccsdk/data/properties/";
        } else if (!propDir.endsWith("/")) {
            propDir = propDir + "/";
        }

        LOG.debug("Initialization complete for " + appName);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.debug("AddCMHandleProvider Closed");
    }

    @Override
    public ListenableFuture<RpcResult<AddCMHandleOutput>> addCMHandle(AddCMHandleInput input) {

        return null;
    }

}

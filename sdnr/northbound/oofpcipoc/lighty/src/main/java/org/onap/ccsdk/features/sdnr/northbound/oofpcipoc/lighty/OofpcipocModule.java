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
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.OofpcipocClient;
import org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.OofpcipocProvider;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the oofpcipoc-provider artifact.
 */
public class OofpcipocModule extends AbstractLightyModule {

    private final SvcLogicService svcLogicService;
    private final DataBroker dataBroker;
    private final NotificationPublishService notificationPublishService;
    private final RpcProviderRegistry rpcProviderRegistry;

    private OofpcipocClient oofpcipocClient;
    private OofpcipocProvider oofpcipocProvider;

    public OofpcipocModule(SvcLogicService svcLogicService, DataBroker dataBroker,
            NotificationPublishService notificationPublishService, RpcProviderRegistry rpcProviderRegistry) {
        this.svcLogicService = svcLogicService;
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    @Override
    protected boolean initProcedure() {
        oofpcipocClient = new OofpcipocClient(svcLogicService);
        oofpcipocProvider = new OofpcipocProvider(dataBroker, notificationPublishService, rpcProviderRegistry, oofpcipocClient);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }
}

/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.rpc;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.config.ORanDMConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanSupervisionRPCImpl {

    private static final Logger LOG = LoggerFactory.getLogger(ORanSupervisionRPCImpl.class);
    private static NodeIdentifier inputNodeIdentifier =
            NodeIdentifier.create(QName.create(ORanDeviceManagerQNames.ORAN_SUPERVISION_MODULE, "input"));
    private static NodeIdentifier supervisionNotificationIntervalIdentifier = NodeIdentifier
            .create(QName.create(ORanDeviceManagerQNames.ORAN_SUPERVISION_MODULE, "supervision-notification-interval"));
    private static NodeIdentifier guardTimerOverheadIdentifier = NodeIdentifier
            .create(QName.create(ORanDeviceManagerQNames.ORAN_SUPERVISION_MODULE, "guard-timer-overhead"));

    public static void invokeWatchdogReset(@NonNull NetconfDomAccessor netconfDomAccessor,
            ORanDMConfig oruSupervisionConfig) {

        LOG.debug(
                "Resetting suppervision-notification-interval and guard-timer-overhead watchdog timers with values {} and {} respectively",
                oruSupervisionConfig.getNotificationInterval(), oruSupervisionConfig.getWatchdogTimer());
        ContainerNode rpcInputNode = Builders.containerBuilder().withNodeIdentifier(inputNodeIdentifier)
                .withChild(ImmutableNodes.leafNode(supervisionNotificationIntervalIdentifier,
                        oruSupervisionConfig.getNotificationInterval()))
                .withChild(
                        ImmutableNodes.leafNode(guardTimerOverheadIdentifier, oruSupervisionConfig.getWatchdogTimer()))
                .build();

        try {
            DOMRpcService rpcService = netconfDomAccessor.getRpcService();
            QName supervisionWatchdogResetQN =
                    QName.create(ORanDeviceManagerQNames.ORAN_SUPERVISION_MODULE, "supervision-watchdog-reset");

            ListenableFuture<? extends DOMRpcResult> result =
                    rpcService.invokeRpc(supervisionWatchdogResetQN, rpcInputNode);
            DOMRpcResult rpcResult = result.get(60000, TimeUnit.MILLISECONDS);
            if (rpcResult.value() != null) {
                ContainerNode rpcResultCn = (ContainerNode) rpcResult.value();
                LOG.debug("Result of Supervision-Watchdog-Reset = {}", rpcResultCn.prettyTree());
            }
        } catch (Exception e) {
            LOG.error("{}", e);
        }
        return;

    }

}

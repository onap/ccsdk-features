/**
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
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.NotificationsService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.StreamNameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfNotification {

    private static final Logger log = LoggerFactory.getLogger(DeviceManagerImpl.class);

    /**
     * Do the stream creation for the device.
     * @param nodeId node-id of device
     * @param mountpoint information
     * @param streamName to register
     */
    public static void registerNotificationStream(String nodeId, MountPoint mountpoint, String streamName) {

        final Optional<RpcConsumerRegistry> optionalRpcConsumerService =
                mountpoint.getService(RpcConsumerRegistry.class);
        if (optionalRpcConsumerService.isPresent()) {
            final RpcConsumerRegistry rpcConsumerRegitry = optionalRpcConsumerService.get();
            @Nonnull
            final NotificationsService rpcService = rpcConsumerRegitry.getRpcService(NotificationsService.class);

            final CreateSubscriptionInputBuilder createSubscriptionInputBuilder = new CreateSubscriptionInputBuilder();
            createSubscriptionInputBuilder.setStream(new StreamNameType(streamName));
            log.info("Event listener triggering notification stream {} for node {}", streamName, nodeId);
            try {
                CreateSubscriptionInput createSubscriptionInput = createSubscriptionInputBuilder.build();
                if (createSubscriptionInput == null) {
                    log.warn("createSubscriptionInput is null for mountpoint {}", nodeId);
                } else {
                    rpcService.createSubscription(createSubscriptionInput);
                }
            } catch (NullPointerException e) {
                log.warn("createSubscription failed");
            }
        } else {
            log.warn("No RpcConsumerRegistry avaialble.");
        }

    }

}

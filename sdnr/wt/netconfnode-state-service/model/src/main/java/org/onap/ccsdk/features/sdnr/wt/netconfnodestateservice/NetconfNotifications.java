/*
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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Interface handling netconf connection.
 */
public interface NetconfNotifications {

    static String DefaultNotificationsStream = "NETCONF";

    /**
     * check if the device supports notifications.yang
     * @return true if notifications.yang is supported
     */
    boolean isNotificationsSupported();

    /**
     * check if the device supports notifications.yang
     * @return true if nc-notifications.yang is supported
     */
    boolean isNCNotificationsSupported();
    /**
     * Get all notification streams
     * @return stream list
     */
    List<Stream> getNotificationStreams();

    /**
     * Register notifications stream for the connection
     *
     * @param streamList that contains a list of streams to be subscribed for notifications
     * @return progress indication
     */
    void registerNotificationsStream(List<Stream> streamList);

    /**
     * Register notifications stream for the connection.
     *
     * @param streamName that should be "NETCONF" as default.
     * @return progress indication
     */
    ListenableFuture<RpcResult<CreateSubscriptionOutput>> registerNotificationsStream(String streamName);

}

/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.adaptermanager.impl;

import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.o.ran.sc.params.xml.ns.yang.nts.manager.rev210608.InstanceChanged;
import org.opendaylight.yang.gen.v1.urn.o.ran.sc.params.xml.ns.yang.nts.manager.rev210608.OperationStatusChanged;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationListenerImpl {

    private static final Logger log = LoggerFactory.getLogger(NotificationListenerImpl.class);
    private final NetconfBindingAccessor netconfAccessor;
    private final DeviceManagerServiceProvider serviceProvider;

    public NotificationListenerImpl(NetconfBindingAccessor netconfAccess,
            DeviceManagerServiceProvider serviceProvider) {
        this.netconfAccessor = netconfAccess;
        this.serviceProvider = serviceProvider;

    }


    public void onInstanceChanged(InstanceChanged notification) {
        log.debug("Got event of type :: InstanceChanged");
        this.serviceProvider.getWebsocketService().sendNotification(notification, netconfAccessor.getNodeId(),
                InstanceChanged.QNAME);
    }


    public void onOperationStatusChanged(OperationStatusChanged notification) {
        log.debug("Got event of type :: OperationStatusChanged");
        this.serviceProvider.getWebsocketService().sendNotification(notification, netconfAccessor.getNodeId(),
                OperationStatusChanged.QNAME);
    }

    public Registration registerNotifications() {
        final var notificationRegistrations = List.of(
                this.netconfAccessor.doRegisterNotificationListener(InstanceChanged.class,
                        NotificationListenerImpl.this::onInstanceChanged),
                this.netconfAccessor.doRegisterNotificationListener(OperationStatusChanged.class,
                        NotificationListenerImpl.this::onOperationStatusChanged)
        );
        return () -> notificationRegistrations.forEach(e -> e.close());
    }
}

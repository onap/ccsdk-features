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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl;

import java.util.List;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfCapabilityChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfigChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfirmedCommit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionEnd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionStart;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.Edit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Shabnam Sultana
 * <p>
 * Listener for change notifications
 **/
public class OpenroadmChangeNotificationListener {

    // variables
    private static final Logger LOG = LoggerFactory.getLogger(OpenroadmChangeNotificationListener.class);
    private final NetconfAccessor netconfAccessor;
    private final DataProvider databaseService;
    private final WebsocketManagerService notificationServiceService;
    // end of variables

    // constructors
    public OpenroadmChangeNotificationListener(NetconfAccessor netconfAccessor, DataProvider databaseService,
            WebsocketManagerService notificationService) {
        this.netconfAccessor = netconfAccessor;
        this.databaseService = databaseService;
        this.notificationServiceService = notificationService;
    }
    // end of constructors

    // public methods

    public void onNetconfConfirmedCommit(NetconfConfirmedCommit notification) {
        LOG.debug("onNetconfConfirmedCommit {} ", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfConfirmedCommit.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }


    public void onNetconfSessionStart(NetconfSessionStart notification) {
        LOG.debug("onNetconfSessionStart {} ", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfSessionStart.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());

    }

    public void onNetconfSessionEnd(NetconfSessionEnd notification) {
        LOG.debug("onNetconfSessionEnd {}", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfSessionEnd.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    public void onNetconfCapabilityChange(NetconfCapabilityChange notification) {
        LOG.debug("onNetconfCapabilityChange {}", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfCapabilityChange.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    public void onNetconfConfigChange(NetconfConfigChange notification) {
        LOG.debug("onNetconfConfigChange (1) {}", notification);
        StringBuffer sb = new StringBuffer();
        List<Edit> editList = notification.nonnullEdit();
        for (Edit edit : editList) {
            if (edit == null) { //should never happen
                LOG.warn("null object in config change");
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            try {
                sb.append(edit);
            } catch (Exception e) { //catch odl error
                LOG.warn("unable to serialize edit obj", e);
            }
            EventlogBuilder eventlogBuilder = new EventlogBuilder();

            var target = edit.getTarget();
            if (target != null) {
                eventlogBuilder.setObjectId(target.toString());
                LOG.debug("TARGET: {}", target.getClass());
                var it = target.steps().iterator();
                while(it.hasNext()) {
                    LOG.debug("PathArgument {}", it.next());
                }
            }
            eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue());
            eventlogBuilder.setNewValue(String.valueOf(edit.getOperation()));
            databaseService.writeEventLog(eventlogBuilder.build());
        }
        LOG.debug("onNetconfConfigChange (2) {}", sb);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfConfigChange.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());

    }

    public Registration registerNotificationListener(MountPoint mountpoint) {
        final Optional<NotificationService> optionalNotificationService =
                mountpoint.getService(NotificationService.class);
        if (optionalNotificationService.isEmpty()) {
            LOG.warn("unable to get notification service for node {}. cannot register for notifications",
                    this.netconfAccessor.getNodeId().getValue());
            return () -> {
            };
        }
        final NotificationService notificationService = optionalNotificationService.get();
        final var listenerRegistrations = List.of(
                notificationService.registerListener(NetconfSessionStart.class,
                        OpenroadmChangeNotificationListener.this::onNetconfSessionStart),
                notificationService.registerListener(NetconfSessionEnd.class,
                        OpenroadmChangeNotificationListener.this::onNetconfSessionEnd),
                notificationService.registerListener(NetconfConfirmedCommit.class,
                        OpenroadmChangeNotificationListener.this::onNetconfConfirmedCommit),
                notificationService.registerListener(NetconfCapabilityChange.class,
                        OpenroadmChangeNotificationListener.this::onNetconfCapabilityChange),
                notificationService.registerListener(NetconfConfigChange.class,
                        OpenroadmChangeNotificationListener.this::onNetconfConfigChange)
        );
        return () -> listenerRegistrations.forEach(e->e.close());
    }
    // end of public methods
}

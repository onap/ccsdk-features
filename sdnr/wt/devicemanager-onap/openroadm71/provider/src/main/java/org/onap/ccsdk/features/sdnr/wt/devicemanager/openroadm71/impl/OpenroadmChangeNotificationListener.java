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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.impl;

import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.IetfNetconfNotificationsListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfCapabilityChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfigChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfirmedCommit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionEnd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionStart;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.Edit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Shabnam Sultana
 *
 *         Listener for change notifications
 *
 **/
public class OpenroadmChangeNotificationListener implements IetfNetconfNotificationsListener {

    // variables
    private static final Logger log = LoggerFactory.getLogger(OpenroadmChangeNotificationListener.class);
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
    @Override
    public void onNetconfConfirmedCommit(NetconfConfirmedCommit notification) {
        log.info("onNetconfConfirmedCommit {} ", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfConfirmedCommit.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    @Override
    public void onNetconfSessionStart(NetconfSessionStart notification) {
        log.info("onNetconfSessionStart {} ", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfSessionStart.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());

    }

    @Override
    public void onNetconfSessionEnd(NetconfSessionEnd notification) {
        log.info("onNetconfSessionEnd {}", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfSessionEnd.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    @Override
    public void onNetconfCapabilityChange(NetconfCapabilityChange notification) {
        log.info("onNetconfCapabilityChange {}", notification);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfCapabilityChange.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    @Override
    public void onNetconfConfigChange(NetconfConfigChange notification) {
        log.info("onNetconfConfigChange (1) {}", notification);
        StringBuffer sb = new StringBuffer();
        List<Edit> editList = notification.nonnullEdit();
        for (Edit edit : editList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(edit);

            EventlogBuilder eventlogBuilder = new EventlogBuilder();

            InstanceIdentifier<?> target = edit.getTarget();
            if (target != null) {
                eventlogBuilder.setObjectId(target.toString());
                log.info("TARGET: {} {}", target.getClass(), target.getTargetType());
                for (PathArgument pa : target.getPathArguments()) {
                    log.info("PathArgument {}", pa);
                }
            }
            eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue());
            eventlogBuilder.setNewValue(String.valueOf(edit.getOperation()));
            databaseService.writeEventLog(eventlogBuilder.build());
        }
        log.info("onNetconfConfigChange (2) {}", sb);
        this.notificationServiceService.sendNotification(notification, this.netconfAccessor.getNodeId(),
                NetconfConfigChange.QNAME, NetconfTimeStampImpl.getConverter().getTimeStamp());

    }

    // end of public methods

}

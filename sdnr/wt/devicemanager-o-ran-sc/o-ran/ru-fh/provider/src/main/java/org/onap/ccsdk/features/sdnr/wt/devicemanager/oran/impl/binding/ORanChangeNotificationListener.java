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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.binding;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationProxyParser;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESNotificationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.IetfNetconfNotificationsListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfCapabilityChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfigChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfirmedCommit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionEnd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionStart;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.Edit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for change notifications
 */
public class ORanChangeNotificationListener implements IetfNetconfNotificationsListener {

    private static final Logger log = LoggerFactory.getLogger(ORanChangeNotificationListener.class);

    private final NetconfBindingAccessor netconfAccessor;
    private final DataProvider databaseService;
    private final NotificationService notificationService;
    private final VESCollectorService vesCollectorService;
    private final NotificationProxyParser notificationProxyParser;
    private ORanNotifToVESEventAssembly mapper = null;

    private static int sequenceNo = 0;

    public ORanChangeNotificationListener(NetconfBindingAccessor netconfAccessor,
            DeviceManagerServiceProvider serviceProvider) {
        this.netconfAccessor = netconfAccessor;
        this.databaseService = serviceProvider.getDataProvider();
        this.notificationService = serviceProvider.getNotificationService();
        this.vesCollectorService = serviceProvider.getVESCollectorService();
        this.notificationProxyParser = vesCollectorService.getNotificationProxyParser();
    }

    @Override
    public void onNetconfConfirmedCommit(NetconfConfirmedCommit notification) {
        log.info("onNetconfConfirmedCommit {}", notification);
    }

    @Override
    public void onNetconfSessionStart(NetconfSessionStart notification) {
        log.info("onNetconfSessionStart {}", notification);
    }

    @Override
    public void onNetconfSessionEnd(NetconfSessionEnd notification) {
        log.info("onNetconfSessionEnd {}", notification);
    }

    @Override
    public void onNetconfCapabilityChange(NetconfCapabilityChange notification) {
        log.info("onNetconfCapabilityChange {}", notification);
    }

    @Override
    public void onNetconfConfigChange(NetconfConfigChange notification) {
        log.info("onNetconfConfigChange (1) {}", notification.toString());

        StringBuffer sb = new StringBuffer();
        List<Edit> editList = notification.nonnullEdit();
        for (Edit edit : editList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(edit);

            InstanceIdentifier<?> target = edit.getTarget();
            if (target != null) {
                log.info("TARGET: {} {}", target.getClass(), target.getTargetType());
                for (PathArgument pa : target.getPathArguments()) {
                    log.info("PathArgument {} Type {}", pa, pa.getType().getFields());
                }

                EventlogEntity eventLogEntity1 = new EventlogBuilder().setNodeId(netconfAccessor.getNodeId().getValue())
                        .setCounter(sequenceNo++).setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp())
                        .setObjectId(target.getTargetType().getCanonicalName()).setAttributeName("N.A")
                        .setSourceType(SourceType.Netconf).setNewValue(String.valueOf(edit.getOperation())).build();
                databaseService.writeEventLog(eventLogEntity1);
            }
        }
        log.info("onNetconfConfigChange (2) {}", sb);

        if (vesCollectorService.getConfig().isVESCollectorEnabled()) {
            if (mapper == null) {
                this.mapper = new ORanNotifToVESEventAssembly(netconfAccessor, vesCollectorService);
            }
            VESCommonEventHeaderPOJO header =
                    mapper.createVESCommonEventHeader(notificationProxyParser.getTime(notification),
                            NetconfConfigChange.class.getSimpleName(), sequenceNo);
            VESNotificationFieldsPOJO body =
                    mapper.createVESNotificationFields(notificationProxyParser.parseNotificationProxy(notification),
                            NetconfConfigChange.class.getSimpleName());
            try {
                vesCollectorService.publishVESMessage(vesCollectorService.generateVESEvent(header, body));
            } catch (JsonProcessingException e) {
                log.warn("Exception while generating JSON object ", e);

            }
        }

    }
}

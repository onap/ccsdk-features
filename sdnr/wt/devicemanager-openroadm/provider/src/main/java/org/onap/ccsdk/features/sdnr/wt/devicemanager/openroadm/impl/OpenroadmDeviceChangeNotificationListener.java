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
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.OrgOpenroadmAlarmListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.ChangeNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.CreateTechInfoNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDeviceListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OtdrScanResult;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.change.notification.Edit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SourceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Listener for Open roadm device specific change notifications
 **/

public class OpenroadmDeviceChangeNotificationListener implements OrgOpenroadmDeviceListener {
    // variables
    private static final Logger log = LoggerFactory.getLogger(OrgOpenroadmAlarmListener.class);
    private Integer counter = 1;
    private final NetconfAccessor netconfAccessor;
    private final DataProvider databaseProvider;
    // end of variables

    // constructors
    public OpenroadmDeviceChangeNotificationListener(NetconfAccessor netconfAccessor, DataProvider databaseService) {
        this.netconfAccessor = netconfAccessor;
        this.databaseProvider = databaseService;
    }
    // end of constructors

    // public methods
    @Override
    public void onOtdrScanResult(OtdrScanResult notification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onChangeNotification(ChangeNotification notification) {
        log.info("onDeviceConfigChange(1){}", notification);
        StringBuffer sb = new StringBuffer();

        @NonNull
        List<Edit> editList = notification.nonnullEdit();
        for (Edit edit : editList) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(edit);

            EventlogBuilder eventlogBuilder = new EventlogBuilder();

            InstanceIdentifier<?> target = edit.getTarget();
            if (target != null) {
                eventlogBuilder.setObjectId(target.getPathArguments().toString());
                log.info("TARGET: {} {}", target.getClass(), target.getTargetType());
                for (PathArgument pa : target.getPathArguments()) {
                    log.info("PathArgument {}", pa);
                }
                eventlogBuilder.setAttributeName(target.getTargetType().getName());
            }
            eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue());

            eventlogBuilder.setNewValue(String.valueOf(edit.getOperation()));
            eventlogBuilder.setTimestamp(notification.getChangeTime());
            eventlogBuilder.setCounter(counter);
            eventlogBuilder.setSourceType(SourceType.Netconf);
            databaseProvider.writeEventLog(eventlogBuilder.build());

            log.info("onDeviceConfigChange (2) {}", sb);
            counter++;
        }
    }

    @Override
    public void onCreateTechInfoNotification(CreateTechInfoNotification notification) {
        // TODO Auto-generated method stub
        log.info("onCreateTechInfoNotification(1){}", notification);

        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setId(notification.getShelfId()).setAttributeName(notification.getShelfId())
                .setObjectId(notification.getShelfId()).setNodeId(this.netconfAccessor.getNodeId().getValue())
                .setCounter(counter).setNewValue(notification.getStatus().getName()).setSourceType(SourceType.Netconf);
        databaseProvider.writeEventLog(eventlogBuilder.build());
        log.info("Create-techInfo Notification written ");
        counter++;

    }
    // end of public methods

}

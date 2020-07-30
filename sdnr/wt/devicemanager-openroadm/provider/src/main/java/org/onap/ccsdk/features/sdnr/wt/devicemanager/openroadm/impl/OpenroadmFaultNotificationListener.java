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


import org.eclipse.jdt.annotation.NonNull;
//import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.AlarmNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.OrgOpenroadmAlarmListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.Severity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Listener for Open roadm device specific alarm notifications
 **/
public class OpenroadmFaultNotificationListener implements OrgOpenroadmAlarmListener {
    private static final Logger log = LoggerFactory.getLogger(OrgOpenroadmAlarmListener.class);
    //private final NetconfAccessor accesor;
    //private final DataProvider databaseProvider;
    private final @NonNull FaultService faultEventListener;
    private Integer count = 1;

    public OpenroadmFaultNotificationListener(NetconfAccessor netConfAccessor,
            DeviceManagerServiceProvider serviceProvider) {
        //this.databaseProvider = serviceProvider.getDataProvider();
        //this.accesor = netConfAccessor;
        this.faultEventListener = serviceProvider.getFaultService();

    }

    @Override
    public void onAlarmNotification(AlarmNotification notification) {

        log.info("AlarmNotification {} \t {}", notification.getId(), notification.getAdditionalDetail());

        FaultlogEntity faultAlarm = new FaultlogBuilder().setObjectId(notification.getCircuitId())
                .setProblem(notification.getProbableCause().getCause().getName())
                .setTimestamp(notification.getRaiseTime()).setId(notification.getId())
                .setNodeId(notification.getResource().getDevice().getNodeId().getValue())
                .setSeverity(checkSeverityValue(notification.getSeverity())).setCounter(count).build();

        //this.databaseProvider.writeFaultLog(faultAlarm);
        this.faultEventListener.faultNotification(faultAlarm);
        count++;
        log.info("Notification is written into the database {}", faultAlarm.getObjectId());

    }


    // Mapping Severity of AlarmNotification to SeverityType of FaultLog
    private SeverityType checkSeverityValue(Severity severity) {
        SeverityType severityType = null;
        log.info("Device Severity: {}", severity.getName());

        switch (severity.getName()) {
            case ("warning"):
                severityType = SeverityType.Warning;
                break;
            case ("major"):
                severityType = SeverityType.Major;
                break;
            case ("minor"):
                severityType = SeverityType.Minor;
                break;
            case ("clear"):
                severityType = SeverityType.NonAlarmed;
                break;
            case ("critical"):
                severityType = SeverityType.Critical;
                break;
            case ("indeterminate"):
                severityType = SeverityType.Critical;
                break;
            default:
                severityType = SeverityType.Critical;
                break;
        }
        return severityType;

    }

}

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

import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.ActiveAlarmList;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.Severity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.active.alarm.list.ActiveAlarms;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Faultlog;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Class to read the initial alarms at the time of device registration
 *
 **/

public class InitialDeviceAlarmReader {
    // variables
    private Integer count = 1;
    private static final Logger log = LoggerFactory.getLogger(InitialDeviceAlarmReader.class);
    private final NetconfBindingAccessor netConfAccesor;
    private final @NonNull FaultService faultEventListener;
    private final DataProvider dataProvider;
    // end of variables

    // constructors
    public InitialDeviceAlarmReader(NetconfBindingAccessor accessor, DeviceManagerServiceProvider serviceProvider) {
        this.netConfAccesor = accessor;
        this.faultEventListener = serviceProvider.getFaultService();
        this.dataProvider = serviceProvider.getDataProvider();
    }
    // end of constructors

    // protected methods
    // Mapping the alarm data with the fault data
    protected FaultData writeFaultData() {
        FaultData faultData = new FaultData();
        ActiveAlarmList actAlarmList = this.getActiveAlarmList(this.netConfAccesor);
        if (actAlarmList != null) {
            Collection<ActiveAlarms> activeAlarms = YangHelper.getCollection(actAlarmList.getActiveAlarms());
            if (!activeAlarms.isEmpty()) {
                for (ActiveAlarms activeAlarm : activeAlarms) {
                    faultData.add(this.netConfAccesor.getNodeId(), this.count, activeAlarm.getRaiseTime(),
                            activeAlarm.getResource().getDevice().getNodeId().getValue(),
                            activeAlarm.getProbableCause().getCause().getName(),
                            checkSeverityValue(activeAlarm.getSeverity()));
                    count = count + 1;
                }
                return faultData;
            }
        }
        return faultData;
    }

    // Write into the FaultLog
    protected void writeAlarmLog(FaultData faultData) {
        if (faultData != null) {
            List<Faultlog> faultLog = faultData.getProblemList();
            for (Faultlog fe : faultLog) {
                this.dataProvider.writeFaultLog(fe);
            }
        }
    }

    // Use the FaultService for Alarm notifications
    protected void faultService() {
        this.faultEventListener.initCurrentProblemStatus(this.netConfAccesor.getNodeId(), writeFaultData());
        writeAlarmLog(writeFaultData());
    }
    // Mapping Severity of AlarmNotification to SeverityType of FaultLog
    protected static SeverityType checkSeverityValue(Severity severity) {
        SeverityType severityType = null;
        log.debug("Device Severity: {}", severity.getName());

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
    // end of protected methods

    // private methods

    // Read Alarm Data
    private ActiveAlarmList getActiveAlarmList(NetconfBindingAccessor accessor) {
        final Class<ActiveAlarmList> classAlarm = ActiveAlarmList.class;
        log.debug("Get Alarm data for element {}", accessor.getNodeId().getValue());
        InstanceIdentifier<ActiveAlarmList> alarmDataIid = InstanceIdentifier.builder(classAlarm).build();

        ActiveAlarmList alarmData = accessor.getTransactionUtils().readData(accessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, alarmDataIid);

        log.debug("AlarmData {}", alarmData);
        return alarmData;
    }


    // end of private methods



}

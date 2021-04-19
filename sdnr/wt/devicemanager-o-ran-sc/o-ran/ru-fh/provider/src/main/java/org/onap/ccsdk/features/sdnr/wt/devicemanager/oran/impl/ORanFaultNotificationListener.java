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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESFaultFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.Alarm.FaultSeverity;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.AlarmNotif;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.ORanFmListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author herbert
 *
 */
public class ORanFaultNotificationListener implements ORanFmListener {

    private static final Logger log = LoggerFactory.getLogger(ORanFaultNotificationListener.class);
    private NetconfBindingAccessor netconfAccessor;
    private DataProvider databaseService;
    private VESCollectorService vesCollectorService;
    private int counter = 0;
    private ORanFaultToVESFaultMapper mapper = null;

    public ORanFaultNotificationListener(NetconfBindingAccessor netconfAccessor, DataProvider databaseService,
            VESCollectorService vesCollectorService) {
        this.netconfAccessor = netconfAccessor;
        this.databaseService = databaseService;
        this.vesCollectorService = vesCollectorService;
    }

    @Override
    public void onAlarmNotif(AlarmNotif notification) {

        log.info("onAlarmNotif {}", notification.getClass().getSimpleName());
        @Nullable
        DateAndTime eventTime = notification.getEventTime();
        try {
            Instant eventTimeInstant = Instant.parse(eventTime.getValue());

            FaultcurrentBuilder faultCurrent = new FaultcurrentBuilder();
            faultCurrent.setNodeId(netconfAccessor.getNodeId().getValue());
            faultCurrent.setObjectId(notification.getFaultSource());
            faultCurrent.setProblem(notification.getFaultText());
            faultCurrent.setSeverity(getSeverityType(notification.getFaultSeverity()));
            faultCurrent.setCounter(Integer.valueOf(counter++));
            faultCurrent.setId(notification.getFaultId().toString());
            faultCurrent.setTimestamp(eventTime);

            databaseService.updateFaultCurrent(faultCurrent.build());

            if (vesCollectorService.getConfig().isVESCollectorEnabled()) {
                if (mapper == null) {
                    this.mapper = new ORanFaultToVESFaultMapper(netconfAccessor.getNodeId(), vesCollectorService,
                            AlarmNotif.class.getSimpleName());
                }
                VESCommonEventHeaderPOJO header =
                        mapper.mapCommonEventHeader(notification, eventTimeInstant, counter);
                VESFaultFieldsPOJO body = mapper.mapFaultFields(notification);
                vesCollectorService.publishVESMessage(vesCollectorService.generateVESEvent(header, body));
            }
        } catch (JsonProcessingException | DateTimeParseException e) {
            log.debug("Can not convert event into VES message {}", notification, e);
        }

    }

    private SeverityType getSeverityType(FaultSeverity faultSeverity) {
        String severity = faultSeverity.getName();
        switch (severity) {
            case "CRITICAL":
                return SeverityType.Critical;
            case "MAJOR":
                return SeverityType.Major;
            case "MINOR":
                return SeverityType.Minor;
            case "WARNING":
                return SeverityType.Warning;
            default:
                return SeverityType.NonAlarmed;
        }
    }

}

/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.notifications;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.Alarms10;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14DomAlarmsNotificationListener implements DOMNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(Onf14DomAlarmsNotificationListener.class);

    private final DeviceManagerServiceProvider serviceProvider;
    private final Alarms10 alarms10;

    public Onf14DomAlarmsNotificationListener(NetconfDomAccessor netconfDomAccessor,
            DeviceManagerServiceProvider serviceProvider, Alarms10 alarms10) {
        this.serviceProvider = serviceProvider;
        this.alarms10 = alarms10;
    }

    @Override
    public void onNotification(@NonNull DOMNotification domNotification) {
        log.debug("In AlarmsNotificationListener - Got event of type :: {} {}", domNotification.getType(),
                domNotification.getBody());
        if (alarms10.isAlarmEventNotification(domNotification)) {
            onAlarmEventNotification(domNotification);
        } else {
            log.error("Unknown notification received - {}", domNotification.getType());
        }
    }

    private void onAlarmEventNotification(@NonNull DOMNotification domNotification) {
        ContainerNode cn = domNotification.getBody();

        FaultlogEntity faultAlarm = alarms10.getFaultlogEntity(cn);
        serviceProvider.getFaultService().faultNotification(faultAlarm);
        alarms10.sendNotification(serviceProvider.getWebsocketService(), domNotification, cn);
        log.debug("onAlarmEventNotification log entry written");

    }

}

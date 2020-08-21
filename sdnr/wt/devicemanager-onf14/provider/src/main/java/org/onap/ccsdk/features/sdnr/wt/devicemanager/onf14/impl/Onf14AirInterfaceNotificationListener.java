/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.AirInterface20Listener;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ProblemNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14AirInterfaceNotificationListener implements AirInterface20Listener {

    private static final Logger log = LoggerFactory.getLogger(Onf14AirInterfaceNotificationListener.class);

    private final NetconfAccessor netconfAccessor;
    private final DeviceManagerServiceProvider serviceProvider;

    public Onf14AirInterfaceNotificationListener(NetconfAccessor netconfAccessor,
            DeviceManagerServiceProvider serviceProvider) {
        this.netconfAccessor = netconfAccessor;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void onObjectDeletionNotification(ObjectDeletionNotification notification) {
        // TODO Auto-generated method stub
        // this type of notification is not yet supported
        log.debug("Got event of type :: {}", ObjectDeletionNotification.class.getSimpleName());
    }

    @Override
    public void onProblemNotification(ProblemNotification notification) {
        log.debug("Got event of type :: {}", ProblemNotification.class.getSimpleName());

        serviceProvider.getFaultService().faultNotification(netconfAccessor.getNodeId(), notification.getCounter(),
                notification.getTimestamp(), notification.getObjectIdRef().getValue(), notification.getProblem(),
                Onf14NetworkElement.mapSeverity(notification.getSeverity()));

    }

    @Override
    public void onAttributeValueChangedNotification(AttributeValueChangedNotification notification) {
        // TODO Auto-generated method stub
        // this type of notification is not yet supported
        log.debug("Got event of type :: {}", AttributeValueChangedNotification.class.getSimpleName());
    }

    @Override
    public void onObjectCreationNotification(ObjectCreationNotification notification) {
        // TODO Auto-generated method stub
        // this type of notification is not yet supported
        log.debug("Got event of type :: {}", ObjectCreationNotification.class.getSimpleName());
    }

}

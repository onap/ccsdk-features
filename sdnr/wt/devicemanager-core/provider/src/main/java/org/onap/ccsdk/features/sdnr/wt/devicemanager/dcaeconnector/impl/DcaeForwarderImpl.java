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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.ProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.MaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DcaeForwarderImpl implements DcaeForwarderInternal, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DcaeForwarderImpl.class);

    private final @Nullable ProviderClient aotsmClient;
    private final ProviderClient dcaeProvider;
    private final MaintenanceService maintenanceService;

    public DcaeForwarderImpl(@Nullable ProviderClient aotsmClient, @Nullable ProviderClient dcaeProvider,
            @NonNull MaintenanceService maintenanceService) {
        super();

        HtAssert.nonnull(maintenanceService);
        this.aotsmClient = aotsmClient;
        this.dcaeProvider = dcaeProvider;
        this.maintenanceService = maintenanceService;
    }

    @Override
    @SuppressWarnings("null")
    public void sendProblemNotificationUsingMaintenanceFilter(String nodeId, ProblemNotificationXml notificationXml) {
        if (!this.maintenanceService.isONFObjectInMaintenance(nodeId, notificationXml.getObjectId(),
                notificationXml.getProblem())) {
            if (dcaeProvider != null) {
                this.dcaeProvider.sendProblemNotification(nodeId, notificationXml);
            }
            if (this.aotsmClient != null) {
                this.aotsmClient.sendProblemNotification(nodeId, notificationXml);
            }
        } else {
            LOG.debug(
                    "Notification will not be sent to external services. Device " + nodeId + " is in maintenance mode");
        }
    }

    @Override
    public void sendProblemNotification(String nodeId, ProblemNotificationXml notificationXml) {
        //to prevent push alarms on reconnect
        //=> only pushed alarms are forwared to dcae
        if (dcaeProvider != null) {
            dcaeProvider.sendProblemNotification(nodeId, notificationXml);
        }
        if (aotsmClient != null) {
            aotsmClient.sendProblemNotification(nodeId, notificationXml);
        }

    }

    @Override
    public void close() throws Exception {}

}

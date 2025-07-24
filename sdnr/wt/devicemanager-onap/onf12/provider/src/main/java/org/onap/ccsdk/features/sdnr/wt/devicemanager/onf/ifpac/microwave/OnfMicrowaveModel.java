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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.notifications.NotificationWorker;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.util.ONFLayerProtocolName;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;

public interface OnfMicrowaveModel {

    public void readTheFaultsOfMicrowaveModel(ONFLayerProtocolName lpName, Class<?> lpClass, UniversalId uuid,
            FaultData resultList);

    public Class<?> getClassForLtpExtension(QName qName);

    // -- Frankfurt below

    /**
     * Get performancedata from LTP
     *
     * @param lpName filter for layerprotocol
     * @param lp to get pm from
     * @return the pm data object
     */
    @NonNull
    PerformanceDataLtp getLtpHistoricalPerformanceData(@NonNull ONFLayerProtocolName lpName, @NonNull Lp lp)
            throws InconsistentPMDataException;

    /** Set notificationqueue worker for specific notification that needs to be forwarded **/
    void setNotificationQueue(NotificationWorker<EventlogEntity> notificationQueue);


    Registration registerListeners(NotificationService notificationService);
}

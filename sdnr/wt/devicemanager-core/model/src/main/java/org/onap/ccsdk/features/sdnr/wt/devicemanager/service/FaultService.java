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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.service;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

/**
 *
 */
public interface FaultService extends DeviceManagerService {

    /**
     * Forward received fault information to devicemanager. Devicemanager writes abstracted fault information to
     * database and send abstracted fault information to websocket.
     * For sending specific notifications to a client, additionally use
     * {@link org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService}
     * <ul>
     * <li>write to database faultcurrent FaultlogEntity namespace: urn:opendaylight:params:xml:ns:yang:data-provider
     * <li>write to database faultlog
     * <li>send via websocket as ProblemNotification namespace: urn:opendaylight:params:xml:ns:yang:devicemanager
     * </ul>
     *
     * @param faultNotification to send
     *
     */
    void faultNotification(@NonNull FaultlogEntity faultNotification);

    //void faultNotification(@NonNull NodeId nodeId,Notification notification, QName type, DateAndTime timeStamp);
    /**
     * Remove current problems of node
     *
     * @param nodeId of node
     * @return number of removed entries
     */
    int removeAllCurrentProblemsOfNode(@NonNull NodeId nodeId);

    /**
     * Write initial list of problems of node
     *
     * @param nodeId of node
     * @param resultList
     */
    void initCurrentProblemStatus(@NonNull NodeId nodeId, FaultData resultList);

    /**
     * @param nodeId of node
     * @param objectId of element to be removed
     * @return number of removed items
     */
    int removeObjectsCurrentProblemsOfNode(@NonNull NodeId nodeId, @NonNull String objectId);

}

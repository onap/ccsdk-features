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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

/**
 * Central notification management of devicemanager
 */
public interface NotificationService extends DeviceManagerService {


    /** Event notification to devicemanager. Can be change, create or remove indication **/
    void eventNotification(@NonNull EventlogEntity eventNotification);

    /** create notification for an object **/
    void creationNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId);

    /** create notification of object **/
    void deletionNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId);

    /**
     * change notification of attribute of object
     * 
     * @param nodeId of device
     * @param counter provided
     * @param timeStamp provided
     * @param objectId provided
     * @param attributeName provided
     * @param newValue provided
     */
    void changeNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId, @Nullable String attributeName, @Nullable String newValue);

}

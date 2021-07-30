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
 * Central notification management of devicemanagers.
 * Notifications are forwarded to ODLUX-Clients and written into database.
 * Type {@link #EventlogEntity} contains all information.
 * Basically, all fields have to be provided with meaningful or
 * unique value.
 * <ul>
 * <li>SourceType: Mandatory to choose one of the ENUMS, specified by SDNC software
 * <li>Host Name: Mountpoint name
 * <li>Timestamp: From message or created by controller
 * <li>ObjectId: Unique ID of Object (e.g. device, interface) within namespace device
 * <li>attributeName: Unique ID within namespace object or message about changed value,
 *     presented in ODLUX "Message" column.
 * </ul>
 */
public interface NotificationService extends DeviceManagerService {

    /**
     * Handling of event notification, received by devicemanager.
     * Can be a change, create or remove indication.
     *
     * @param eventNotification is containing all event related information.
     */
    void eventNotification(@NonNull EventlogEntity eventNotification);

    /**
     * create notification for an object. Message set to "Create"
     *
     * @param nodeId of device
     * @param counter provided
     * @param timeStamp provided
     * @param objectId provided
     */
    void creationNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId);

    /**
     * delete notification of object. Message set to "Deletion"
     *
     * @param nodeId of device
     * @param counter provided
     * @param timeStamp provided
     * @param objectId provided
     */
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

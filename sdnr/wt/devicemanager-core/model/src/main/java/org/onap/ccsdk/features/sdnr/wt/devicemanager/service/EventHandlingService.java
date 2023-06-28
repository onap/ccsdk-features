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

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

/**
 * Event Forwarding for central event management by devicemanager
 */
public interface EventHandlingService {

    /**
     * @param mountPointNodeName
     * @param deviceType
     */
    void connectIndication(NodeId nodeId, NetworkElementDeviceType deviceType);

    /**
     * @param mountPointNodeName
     */
    void deRegistration(NodeId nodeId);

    /**
     *
     * @param registrationName
     * @param attribute
     * @param attributeNewValue
     * @param nNode
     */
    void updateRegistration(NodeId nodeId, String attribute, String attributeNewValue, NetconfNode nNode);

    /**
     * @param nodeIdString
     * @param nNode
     */
    void registration(NodeId nodeId, NetconfNode nNode);

    /**
     * @param objectId
     * @param msg
     * @param value
     */
    void writeEventLog(String objectId, String msg, String value);

}

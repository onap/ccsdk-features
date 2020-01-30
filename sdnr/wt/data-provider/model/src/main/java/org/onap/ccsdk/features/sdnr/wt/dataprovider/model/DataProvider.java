/**
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.model;

import java.util.Date;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntity;

public interface DataProvider extends ArchiveCleanProvider {

    // -- Connection log
    void writeConnectionLog(ConnectionlogEntity event);

    // -- Event log
    void writeEventLog(EventlogEntity event);

    void writeFaultLog(FaultlogEntity fault);

    void updateFaultCurrent(FaultcurrentEntity fault);

    /**
     * Remove all entries for one node
     *
     * @param nodeName contains the mountpointname
     * @return number of deleted entries
     */
    int clearFaultsCurrentOfNode(String nodeName);

    /**
     * Remove all entries for one node
     *
     * @param nodeName contains the mountpointname
     * @param objectId of element to be deleted
     * @return number of deleted entries
     */
    int clearFaultsCurrentOfNodeWithObjectId(String nodeName, String objectId);

    /**
     * Deliver list with all mountpoint/node-names in the database.
     *
     * @return List of all mountpoint/node-names the had active alarms.
     */
    List<String> getAllNodesWithCurrentAlarms();

    /**
     * write internal equipment to database
     * @param internalEquipment with mandatory fields.
     */
    void writeInventory(Inventory internalEquipment);

    /**
     *
     * @param networkElementConnectionEntitiy to wirte to DB
     * @param nodeId Id for this DB element
     */
    void updateNetworkConnectionDeviceType(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId);

    /**
     * Update after new mountpoint registration
     * @param networkElementConnectionEntitiy data
     * @param nodeId of device (mountpoint name)
     */
    void updateNetworkConnection22(NetworkElementConnectionEntity networkElementConnectionEntitiy, String nodeId);

    void removeNetworkConnection(String nodeId);

    @Override
    int doIndexClean(Date olderAreOutdated);

    @Override
    int getNumberOfOldObjects(Date olderAreOutdated);

    List<NetworkElementConnectionEntity> getNetworkElementConnections();

    /**
     * @param list
     */
    void doWritePerformanceData(List<PmdataEntity> list);

}

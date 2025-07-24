/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.nodb;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;

public class NoDbDataProvider implements DataProvider {

    @Override
    public int doIndexClean(Date olderAreOutdated) {
        return 0;
    }

    @Override
    public long getNumberOfOldObjects(Date olderAreOutdated) {
        return 0;
    }

    @Override
    public void writeConnectionLog(ConnectionlogEntity event) {

    }

    @Override
    public void writeEventLog(EventlogEntity event) {

    }

    @Override
    public void writeFaultLog(FaultlogEntity fault) {

    }

    @Override
    public void writeCMLog(CmlogEntity cm) {

    }

    @Override
    public void updateFaultCurrent(FaultcurrentEntity fault) {

    }

    @Override
    public int clearFaultsCurrentOfNode(String nodeName) {
        return 0;
    }

    @Override
    public int clearFaultsCurrentOfNodeWithObjectId(String nodeName, String objectId) {
        return 0;
    }

    @Override
    public List<String> getAllNodesWithCurrentAlarms() {
        return Arrays.asList();
    }

    @Override
    public void writeInventory(String nodeId, List<Inventory> list) {
     
    }

    @Override
    public void writeGuiCutThroughData(Guicutthrough gcData, String nodeId) {

    }

    @Override
    public int clearGuiCutThroughEntriesOfNode(String nodeName) {
        return 0;
    }

    @Override
    public boolean updateNetworkConnectionDeviceType(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId) {
        return true;
    }

    @Override
    public boolean updateNetworkConnection22(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId) {
        return true;
    }

    @Override
    public void removeNetworkConnection(String nodeId) {
 
    }

    @Override
    public List<NetworkElementConnectionEntity> getNetworkElementConnections() {
        return Arrays.asList();
    }

    @Override
    public void doWritePerformanceData(List<PmdataEntity> list) {
  
    }
}

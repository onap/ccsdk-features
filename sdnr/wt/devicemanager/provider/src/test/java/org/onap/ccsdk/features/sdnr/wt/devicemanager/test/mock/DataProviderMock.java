/*******************************************************************************
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt sdnr-wt-devicemanager-provider
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntity;

public class DataProviderMock implements DataProvider {

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
        return new ArrayList<>();
    }

    @Override
    public void writeInventory(Inventory internalEquipment) {
    }

    @Override
    public void updateNetworkConnectionDeviceType(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId) {
    }

    @Override
    public void updateNetworkConnection22(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId) {
    }

    @Override
    public void removeNetworkConnection(String nodeId) {
    }

    @Override
    public int doIndexClean(Date olderAreOutdated) {
        return 0;
    }

    @Override
    public int getNumberOfOldObjects(Date olderAreOutdated) {
        return 0;
    }

    @Override
    public List<NetworkElementConnectionEntity> getNetworkElementConnections() {
        return new ArrayList<>();
    }

    @Override
    public void doWritePerformanceData(List<PmdataEntity> list) {
    }
}

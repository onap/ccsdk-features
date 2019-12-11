/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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
}

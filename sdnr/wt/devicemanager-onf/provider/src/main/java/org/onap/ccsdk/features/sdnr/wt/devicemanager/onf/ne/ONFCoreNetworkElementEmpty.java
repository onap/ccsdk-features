/*******************************************************************************
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
 ******************************************************************************/
/**
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ONFCoreNetworkElementRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InventoryInformationDcae;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author herbert
 *
 */
public class ONFCoreNetworkElementEmpty implements ONFCoreNetworkElementRepresentation {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElementEmpty.class);

    private final NetconfAccessor acessor;
    private final String mountPointNodeName;
    private final NodeId nodeId;

    public ONFCoreNetworkElementEmpty(NetconfAccessor acessor, String mountPointNodeName) {
        LOG.info("Create {}",ONFCoreNetworkElementEmpty.class.getSimpleName());
        this.mountPointNodeName = mountPointNodeName;
        this.nodeId = new NodeId(mountPointNodeName);
        this.acessor = acessor;
    }

    @Override
    public void initialReadFromNetworkElement() {
    }

    @Override
    public String getMountPointNodeName() {
        return mountPointNodeName;
    }

    @Override
    public String getMountpoint() {
        return mountPointNodeName;
    }

    @Override
    public void resetPMIterator() {
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public void next() {
    }

    @Override
    public String pmStatusToString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int removeAllCurrentProblemsOfNode() {
        return 0;
    }

    @Override
    public void doRegisterEventListener(MountPoint mointPoint) {
        //Do nothing
    }

    @Override
    public void prepareCheck() {
        //Do nothing here
    }

    @Override
    public boolean checkIfConnectionToMediatorIsOk() {
        return true;
    }

    @Override
    public boolean checkIfConnectionToNeIsOk() {
        return true;
    }

    @Override
    public InventoryInformationDcae getInventoryInformation() {
        return InventoryInformationDcae.getDefault();
    }

    @Override
    public InventoryInformationDcae getInventoryInformation(String layerProtocolFilter) {
        return InventoryInformationDcae.getDefault();
    }

    @Override
    public DataBroker getDataBroker() {
        return null;
    }

    @Override
    public Optional<NetworkElement> getOptionalNetworkElement() {
        return Optional.empty();
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return  NetworkElementDeviceType.Unknown;
    }

    @Override
    public void register() {
    }

    @Override
    public void deregister() {
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public <L extends NetworkElementService> Optional<L> getService(Class<L> clazz) {
        return Optional.empty();
    }

    @Override
    public void warmstart() {
    }

    @Override
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(acessor);
    }

    @Override
    public Optional<PerformanceDataLtp> getLtpHistoricalPerformanceData() {
        return Optional.empty();
    }

}

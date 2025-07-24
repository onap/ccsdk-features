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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.MaintenanceRPCServiceAPI;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.MaintenanceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetMaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.SetMaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.TestMaintenanceModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.show.required.network.element.output.RequiredNetworkElementBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaintenanceServiceImpl implements MaintenanceService, MaintenanceRPCServiceAPI, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MaintenanceServiceImpl.class);

    private final HtDatabaseMaintenance database;

    public MaintenanceServiceImpl(HtDatabaseMaintenance client) {

        LOG.info("Create {} start", MaintenanceServiceImpl.class);
        database = client;
        LOG.info("Create {} finished. DB Service {} started.", MaintenanceServiceImpl.class,
                client != null ? "sucessfully" : "not");

    }

    @Override
    public void createIfNotExists(NodeId nodeId) {
        database.createIfNotExists(nodeId.getValue());
    }

    @Override
    public void deleteIfNotRequired(NodeId nodeId) {
        database.deleteIfNotRequired(nodeId.getValue());
    }

    /*-------------------------------------------------
     * Interface AutoClosable
     */

    @Override
    public void close() throws Exception {}

    /*-------------------------------------------------
     * Interface MaintenanceRPCServiceAPI
     */

    @Override
    public GetRequiredNetworkElementKeysOutputBuilder getRequiredNetworkElementKeys() {
        List<MaintenanceEntity> all = database.getAll();

        Set<String> mountpointList = new HashSet<>();
        for (MaintenanceEntity oneOfAll : all) {
            mountpointList.add(oneOfAll.getNodeId());

        }
        GetRequiredNetworkElementKeysOutputBuilder outputBuilder = new GetRequiredNetworkElementKeysOutputBuilder();
        outputBuilder.setMountpointNames(mountpointList);
        return outputBuilder;
    }

    @Override
    public ShowRequiredNetworkElementOutputBuilder showRequiredNetworkElement(ShowRequiredNetworkElementInput input) {
        ShowRequiredNetworkElementOutputBuilder outputBuilder = new ShowRequiredNetworkElementOutputBuilder();
        MaintenanceEntity maintenanceMode = database.getMaintenance(input.getMountpointName());
        if (maintenanceMode != null) {
            RequiredNetworkElementBuilder valueBuilder = new RequiredNetworkElementBuilder();

            valueBuilder.setMountpointName(maintenanceMode.getNodeId());
            valueBuilder
                    .setStatus(String.valueOf(MaintenanceCalculator.isONFObjectInMaintenance(maintenanceMode, "", "")));
            valueBuilder.setDescription("Pretty description here");
            outputBuilder.setRequiredNetworkElement(valueBuilder.build());
        } else {
            LOG.warn("No info in database for {}", input.getMountpointName());
        }
        return outputBuilder;
    }

    @Override
    public GetMaintenanceModeOutputBuilder getMaintenanceMode(GetMaintenanceModeInput input) {

        GetMaintenanceModeOutputBuilder outputBuilder;
        MaintenanceEntity maintenanceMode = database.getMaintenance(input.getMountpointName());
        if (maintenanceMode != null) {
            outputBuilder = new GetMaintenanceModeOutputBuilder(maintenanceMode);
        } else {
            throw new IllegalArgumentException("No info in database for " + input.getMountpointName());
        }
        return outputBuilder;
    }

    @Override
    public SetMaintenanceModeOutputBuilder setMaintenanceMode(SetMaintenanceModeInput input) {

        SetMaintenanceModeOutputBuilder outputBuilder = new SetMaintenanceModeOutputBuilder();
        MaintenanceBuilder mb = new MaintenanceBuilder(input);
        MaintenanceEntity m = mb.build();
        database.setMaintenance(m);
        return outputBuilder;

    }

    @Override
    public TestMaintenanceModeOutputBuilder testMaintenanceMode(TestMaintenanceModeInput input) {

        StringBuffer resultString = new StringBuffer();

        MaintenanceEntity maintenanceMode = database.getMaintenance(input.getMountpointName());

        ZonedDateTime now = MaintenanceCalculator.valueOf(input.getTestDate());

        resultString.append("In database table: ");
        resultString.append(maintenanceMode != null);
        resultString.append(" Maintenance active: ");
        resultString.append(MaintenanceCalculator.isONFObjectInMaintenance(maintenanceMode, input.getObjectIdRef(),
                input.getProblemName(), now));
        resultString.append(" at Timestamp: ");
        resultString.append(now);
        TestMaintenanceModeOutputBuilder outputBuilder =
                maintenanceMode != null ? new TestMaintenanceModeOutputBuilder(maintenanceMode)
                        : new TestMaintenanceModeOutputBuilder();
        outputBuilder.setResultString(resultString.toString());
        return outputBuilder;

    }

    /*-------------------------------------------------
     * Interface MaintenaceService
     */

    @Override
    public boolean isONFObjectInMaintenance(NodeId nodeId, String objectIdRef, String problem) {
        MaintenanceEntity maintenanceMode = database.getMaintenance(nodeId.getValue());
        boolean res = MaintenanceCalculator.isONFObjectInMaintenance(maintenanceMode, objectIdRef, problem);
        LOG.debug("inMaintenance={} for mountpoint/id/problem:{} {} {} Definition: {}", res, nodeId.getValue(),
                objectIdRef, problem, this);
        return res;
    }

}

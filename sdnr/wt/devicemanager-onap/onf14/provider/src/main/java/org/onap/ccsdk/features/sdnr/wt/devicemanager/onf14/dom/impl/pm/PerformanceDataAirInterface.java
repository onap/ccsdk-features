/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.pm;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.qnames.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.entity.PerformanceDataBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceDataAirInterface extends PmdataEntityBuilder {

    private static final Logger log = LoggerFactory.getLogger(PerformanceDataAirInterface.class);

    public PerformanceDataAirInterface(NodeId nodeId, String ltpUuid, String localId,
            @NonNull DataContainerNode airInterfaceHistPerfEntry) {
        log.debug("Performance Data of Air Interface = {}", airInterfaceHistPerfEntry);
        String leafVal;

        this.setGranularityPeriod(Helper.mapGranularityPeriod(Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerfEntry,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_GP)));
        this.setUuidInterface(ltpUuid);
        this.setLayerProtocolName(localId); // TODO
        this.setNodeName(nodeId.getValue());
        this.setScannerId(Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerfEntry,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_HDI));
        this.setTimeStamp(new DateAndTime(Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerfEntry,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_PET)));
        this.setSuspectIntervalFlag(Boolean.getBoolean(Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerfEntry,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_SIF)));

        PerformanceDataBuilder bPerformanceData = new PerformanceDataBuilder();
        ContainerNode airInterfaceHistPerf = (ContainerNode) airInterfaceHistPerfEntry
                .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_DATA));

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_ES)) != null) {
            bPerformanceData.setEs(Integer.parseInt(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_SES)) != null) {
            bPerformanceData.setSes(Integer.parseInt(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_CSES)) != null) {
            bPerformanceData.setCses(Integer.parseInt(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_UNAVAILABILITY)) != null) {
            bPerformanceData.setUnavailability(Integer.parseInt(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_TX_LEVEL_MIN)) != null) {
            bPerformanceData.setTxLevelMin(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_TX_LEVEL_MAX)) != null) {
            bPerformanceData.setTxLevelMax(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_TX_LEVEL_AVG)) != null) {
            bPerformanceData.setTxLevelAvg(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_RX_LEVEL_MIN)) != null) {
            bPerformanceData.setTxLevelMin(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_RX_LEVEL_MAX)) != null) {
            bPerformanceData.setTxLevelMax(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_RX_LEVEL_AVG)) != null) {
            bPerformanceData.setTxLevelAvg(Byte.parseByte(leafVal));
        }


        //		  //TODO: xstates-list bPerformanceData.setTime2States(pmr.getTime2States());
        //		  bPerformanceData.setTime4StatesS(pmr.getTime4StatesS());
        //		  bPerformanceData.setTime4States(pmr.getTime4States());
        //		  bPerformanceData.setTime8States(pmr.getTime8States());
        //		  bPerformanceData.setTime16StatesS(pmr.getTime16StatesS());
        //		  bPerformanceData.setTime16States(pmr.getTime16States());
        //		  bPerformanceData.setTime32States(pmr.getTime32States());
        //		  bPerformanceData.setTime64States(pmr.getTime64States());
        //		  bPerformanceData.setTime128States(pmr.getTime128States());
        //		  bPerformanceData.setTime256States(pmr.getTime256States());
        //		  bPerformanceData.setTime512States(pmr.getTime512States());
        //		  bPerformanceData.setTime512StatesL(pmr.getTime512StatesL());
        //		  bPerformanceData.setTime1024States(pmr.getTime1024States());
        //		  bPerformanceData.setTime1024StatesL(pmr.getTime1024StatesL());
        //		  bPerformanceData.setTime2048States(pmr.getTime2048States());
        //		  bPerformanceData.setTime2048StatesL(pmr.getTime2048StatesL());
        //		  bPerformanceData.setTime4096States(pmr.getTime4096States());
        //		  bPerformanceData.setTime4096StatesL(pmr.getTime4096StatesL());
        //		  bPerformanceData.setTime8192States(pmr.getTime8192States());
        //		  bPerformanceData.setTime8192StatesL(pmr.getTime8192StatesL());


        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_SNIR_MIN)) != null) {
            bPerformanceData.setSnirMin(Byte.parseByte(leafVal));
        }
        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_SNIR_MAX)) != null) {
            bPerformanceData.setSnirMax(Byte.parseByte(leafVal));
        }
        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_SNIR_AVG)) != null) {
            bPerformanceData.setSnirAvg(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_XPD_MIN)) != null) {
            bPerformanceData.setXpdMin(Byte.parseByte(leafVal));
        }
        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_XPD_MAX)) != null) {
            bPerformanceData.setXpdMax(Byte.parseByte(leafVal));
        }
        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_XPD_AVG)) != null) {
            bPerformanceData.setXpdAvg(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_RF_TEMP_MIN)) != null) {
            bPerformanceData.setRfTempMin(Byte.parseByte(leafVal));
        }
        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_RF_TEMP_MAX)) != null) {
            bPerformanceData.setRfTempMax(Byte.parseByte(leafVal));
        }
        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_RF_TEMP_AVG)) != null) {
            bPerformanceData.setRfTempAvg(Byte.parseByte(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_DEFECT_BLOCKS_SUM)) != null) {
            bPerformanceData.setDefectBlocksSum(Short.valueOf(leafVal));
        }

        if ((leafVal = Onf14DMDOMUtility.getLeafValue(airInterfaceHistPerf,
                Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCE_TIME_PERIOD)) != null) {
            bPerformanceData.setTimePeriod(Integer.parseInt(leafVal));
        }

        this.setPerformanceData(bPerformanceData.build());
    }
}

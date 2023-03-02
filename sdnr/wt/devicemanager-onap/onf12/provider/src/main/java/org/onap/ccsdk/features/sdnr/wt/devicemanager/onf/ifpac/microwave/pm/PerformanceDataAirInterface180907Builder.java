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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.pm;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.Helper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.PmUtil;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.OtnHistoryDataG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev180907.AirInterfaceHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev180907.ContainerHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev180907.air._interface.historical.performance.type.g.PerformanceData;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev180907.mw.air._interface.pac.AirInterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.entity.PerformanceDataBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class PerformanceDataAirInterface180907Builder extends PmdataEntityBuilder {

	private PerformanceDataAirInterface180907Builder(NodeId nodeId, Lp lp, OtnHistoryDataG pmRecord) {
		super();
		Optional<GranularityPeriodType> gp = Optional.ofNullable(GranularityPeriodType
				.forName(Helper.nnGetGranularityPeriodType(pmRecord.getGranularityPeriod()).getName()));
		this.setGranularityPeriod(gp.orElse(GranularityPeriodType.Unknown));
		this.setUuidInterface(Helper.nnGetUniversalId(lp.getUuid()).getValue());
		this.setLayerProtocolName(Helper.nnGetLayerProtocolName(lp.getLayerProtocolName()).getValue());
		this.setNodeName(nodeId.getValue());
		this.setScannerId(pmRecord.getHistoryDataId());
		this.setTimeStamp(pmRecord.getPeriodEndTime());
		this.setSuspectIntervalFlag(pmRecord.requireSuspectIntervalFlag());
	}

	/**
	 * Move data to generic type
	 *
	 * @param nodeId           of node
	 * @param lp               to get data from
	 * @param pmRecord         data itself
	 * @param airConfiguration configuration for additional parameter
	 * @throws InconsistentPMDataException
	 */
	public PerformanceDataAirInterface180907Builder(NodeId nodeId, Lp lp,
			AirInterfaceHistoricalPerformanceTypeG pmRecord, AirInterfaceConfiguration airConfiguration)
			throws InconsistentPMDataException {
		this(nodeId, lp, pmRecord);

		this.setRadioSignalId(airConfiguration.getRadioSignalId());
		@NonNull
		PerformanceData pmr = Helper.throwIfPerformanceDataNull(pmRecord.getPerformanceData(),
				getUuidInterface(), getLayerProtocolName());

		PerformanceDataBuilder bPerformanceData = new PerformanceDataBuilder();
		bPerformanceData.setTimePeriod(pmr.getTimePeriod());
		bPerformanceData.setEs(pmr.getEs());
		bPerformanceData.setSes(pmr.getSes());
		bPerformanceData.setCses(pmr.getCses());
		bPerformanceData.setUnavailability(pmr.getUnavailability());
		bPerformanceData.setTxLevelMin(pmr.getTxLevelMin());
		bPerformanceData.setTxLevelMax(pmr.getTxLevelMax());
		bPerformanceData.setTxLevelAvg(pmr.getTxLevelAvg());
		bPerformanceData.setRxLevelMin(pmr.getRxLevelMin());
		bPerformanceData.setRxLevelMax(pmr.getRxLevelMax());
		bPerformanceData.setRxLevelAvg(pmr.getRxLevelAvg());
		bPerformanceData.setTime2States(pmr.getTime2States());
		bPerformanceData.setTime4StatesS(pmr.getTime4StatesS());
		bPerformanceData.setTime4States(pmr.getTime4States());
		bPerformanceData.setTime8States(pmr.getTime8States());
		bPerformanceData.setTime16StatesS(pmr.getTime16StatesS());
		bPerformanceData.setTime16States(pmr.getTime16States());
		bPerformanceData.setTime32States(pmr.getTime32States());
		bPerformanceData.setTime64States(pmr.getTime64States());
		bPerformanceData.setTime128States(pmr.getTime128States());
		bPerformanceData.setTime256States(pmr.getTime256States());
		bPerformanceData.setTime512States(pmr.getTime512States());
		bPerformanceData.setTime512StatesL(pmr.getTime512StatesL());
		bPerformanceData.setTime1024States(pmr.getTime1024States());
		bPerformanceData.setTime1024StatesL(pmr.getTime1024StatesL());
		bPerformanceData.setTime2048States(pmr.getTime2048States());
		bPerformanceData.setTime2048StatesL(pmr.getTime2048StatesL());
		bPerformanceData.setTime4096States(pmr.getTime4096States());
		bPerformanceData.setTime4096StatesL(pmr.getTime4096StatesL());
		bPerformanceData.setTime8192States(pmr.getTime8192States());
		bPerformanceData.setTime8192StatesL(pmr.getTime8192StatesL());
		bPerformanceData.setSnirMin(pmr.getSnirMin());
		bPerformanceData.setSnirMax(pmr.getSnirMax());
		bPerformanceData.setSnirAvg(pmr.getSnirAvg());
		bPerformanceData.setXpdMin(pmr.getXpdMin());
		bPerformanceData.setXpdMax(pmr.getXpdMax());
		bPerformanceData.setXpdAvg(pmr.getXpdAvg());
		bPerformanceData.setRfTempMin(pmr.getRfTempMin());
		bPerformanceData.setRfTempMax(pmr.getRfTempMax());
		bPerformanceData.setRfTempAvg(pmr.getRfTempAvg());
		bPerformanceData.setDefectBlocksSum(pmr.getDefectBlocksSum());
		this.setPerformanceData(bPerformanceData.build());
	}

	/**
	 * Move data to generic type
	 *
	 * @param nodeId   of node
	 * @param lp       to get data from
	 * @param pmRecord data itself
	 * @throws InconsistentPMDataException
	 */
	public PerformanceDataAirInterface180907Builder(NodeId nodeId, Lp lp, ContainerHistoricalPerformanceTypeG pmRecord)
			throws InconsistentPMDataException {
		this(nodeId, lp, (OtnHistoryDataG) pmRecord);
		org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev180907.container.historical.performance.type.g.@NonNull PerformanceData pmr = Helper
				.throwIfPerformanceDataNull(pmRecord.getPerformanceData(), getUuidInterface(),
						getLayerProtocolName());

		PerformanceDataBuilder bPerformanceData = new PerformanceDataBuilder();
		bPerformanceData.setTimePeriod(pmr.getTimePeriod());
		bPerformanceData.setTxEthernetBytesMaxM(pmr.getTxEthernetBytesMaxM());
		bPerformanceData.setTxEthernetBytesMaxS(pmr.getTxEthernetBytesMaxS());
		bPerformanceData.setTxEthernetBytesSum(pmr.getTxEthernetBytesSum());
		this.setPerformanceData(bPerformanceData.build());
	}

}

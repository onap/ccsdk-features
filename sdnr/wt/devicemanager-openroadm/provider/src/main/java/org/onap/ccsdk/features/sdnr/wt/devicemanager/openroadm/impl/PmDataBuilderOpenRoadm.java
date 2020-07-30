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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl;

import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.HistoricalPmList;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.group.HistoricalPm;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.list.HistoricalPmEntry;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.val.group.Measurement;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmGranularity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.pmdata.entity.PerformanceData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.pmdata.entity.PerformanceDataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shabnam
 *
 *         Reading Openroadm PM data and returning as PmDataEntitiy data
 */
public class PmDataBuilderOpenRoadm {
    // variables
    private static final Logger log = LoggerFactory.getLogger(OpenroadmNetworkElement.class);
    private PmdataEntityBuilder pmDataBuilder;

    // end of variables
    // constructors
    public PmDataBuilderOpenRoadm(NetconfAccessor accessor) {
        this.pmDataBuilder = new PmdataEntityBuilder();
        this.pmDataBuilder.setNodeName(accessor.getNodeId().getValue());
    }

    // end of constructors
    // public methods
    // Read PM data
    public HistoricalPmList getPmData(NetconfAccessor accessor) {
        final Class<HistoricalPmList> pmDataClass = HistoricalPmList.class;
        log.info("Get PM data for element {}", accessor.getNodeId().getValue());
        InstanceIdentifier<HistoricalPmList> pmDataIid = InstanceIdentifier.builder(pmDataClass).build();
        return accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                pmDataIid);

    }

    // Build PM entity for writing into the database
    public List<PmdataEntity> buildPmDataEntity(HistoricalPmList historicalPmEnitityList) {
        List<PmdataEntity> pmEntitiyList = new ArrayList<>();
        List<HistoricalPmEntry> pmDataEntryList = historicalPmEnitityList.getHistoricalPmEntry();
        for (HistoricalPmEntry pmDataEntry : pmDataEntryList) {
            pmDataBuilder.setUuidInterface(pmDataEntry.getPmResourceType().getName());
            List<HistoricalPm> historicalPmList = pmDataEntry.getHistoricalPm();
            for (HistoricalPm historicalPm : historicalPmList) {
                log.info("PmName:{}", historicalPm.getType());
                this.pmDataBuilder.setScannerId(historicalPm.getType().getName());
                writeperformanceData(historicalPm);
                log.info("NodeName: {}, Scanner Id:{}, Period: {}", this.pmDataBuilder.getNodeName(),
                        this.pmDataBuilder.getScannerId(), this.pmDataBuilder.getGranularityPeriod().getName());
                pmEntitiyList.add(this.pmDataBuilder.build());
                log.info("PmListSize before db writing: {}", pmEntitiyList.size());
            }
            log.info("PmListSize before db writing: {}", pmEntitiyList.size());
        }
        return pmEntitiyList;
    }
    // end of public methods

    // private methods
    private void writeperformanceData(HistoricalPm historicalPm) {
        List<Measurement> measurementList = historicalPm.getMeasurement();

        for (Measurement measurementData : measurementList) {
            this.pmDataBuilder.setGranularityPeriod(mapGranularityPeriod(measurementData.getGranularity()))
                    .setPerformanceData(getPerformancedata(measurementData))
                    .setTimeStamp(measurementData.getCompletionTime());
            if (measurementData.getValidity().getName().equals("suspect")) {
                this.pmDataBuilder.setSuspectIntervalFlag(true);
            }
            log.info("Time:d{}, \n Scannerid: {}, \n UUID: {}", this.pmDataBuilder.getGranularityPeriod().getName(),
                    this.pmDataBuilder.getScannerId(), this.pmDataBuilder.getUuidInterface());
        }
    }

    //Map Performance data of PmDataEntity with  MeasurmentData-HistoricalPm
    private PerformanceData getPerformancedata(Measurement measurementData) {
        PerformanceData performanceData;
        PerformanceDataBuilder performanceDataBuilder = new PerformanceDataBuilder();
        performanceData = performanceDataBuilder.setCses(measurementData.getBinNumber())
                .setSes(measurementData.getPmParameterValue().getUint64().intValue()).build();
        return performanceData;
    }

    // Mapping Granularity period of PmDataEntity with PmGranularity of MeasurmentData-HistoricalPm
    private GranularityPeriodType mapGranularityPeriod(PmGranularity pmGranularity) {

        GranularityPeriodType granPeriod = null;
        switch (pmGranularity.getName()) {
            case ("notApplicable"):
                granPeriod = GranularityPeriodType.Unknown;
                break;
            case ("15min"):
                granPeriod = GranularityPeriodType.Period15Min;
                break;
            case ("24Hour"):
                granPeriod = GranularityPeriodType.Period24Hours;
                break;
            default:
                granPeriod = GranularityPeriodType.Period15Min;
                break;
        }
        return granPeriod;
    }
    // end of private methods
}

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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.service;

import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrowaveHistoricalPerformanceWriterService {

    private static final Logger LOG = LoggerFactory.getLogger(MicrowaveHistoricalPerformanceWriterService.class);

    private final DataProvider dataProvider;
//    private HtDataBaseReaderAndWriter<EsHistoricalPerformance15Minutes> historicalPerformance15mRW;
//    private HtDataBaseReaderAndWriter<EsHistoricalPerformance24Hours> historicalPerformance24hRW;
//    private HtDataBaseReaderAndWriter<EsHistoricalPerformanceLogEntry> historicalPerformanceLogRW;

    @Deprecated
    public MicrowaveHistoricalPerformanceWriterService(DataProvider dataProvider) {

        LOG.info("Create {} start", MicrowaveHistoricalPerformanceWriterService.class);
        this.dataProvider = dataProvider;

        LOG.info("Create {} finished. DB Service {} started.", MicrowaveHistoricalPerformanceWriterService.class,
                dataProvider != null ? "sucessfully" : "not");
    }


//    public void writePM(AllPm pm) {
//        LOG.debug("Write {} pm records", pm.size());
//
//        LOG.debug("Write 15m write to DB");
//        historicalPerformance15mRW.doWrite(pm.getPm15());
//        LOG.debug("Write 15m done, Write 24h write to DB");
//        historicalPerformance24hRW.doWrite(pm.getPm24());
//        LOG.debug("Write 24h done");
//    }
//
    /**
     * @param performanceDataLtp
     */
    public void writePM(PerformanceDataLtp performanceDataLtp) {

        dataProvider.doWritePerformanceData(performanceDataLtp.getList());
    }

    static public boolean isAvailable(MicrowaveHistoricalPerformanceWriterService s) {

        if (s == null || s.dataProvider == null) {
            return false;
        }
        return true;
    }



}

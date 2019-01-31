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

import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database.HtDataBaseReaderAndWriter;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database.HtDatabaseClientAbstract;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database.HtDatabaseNode;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database.IndexClientBuilder;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container.AllPm;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.util.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types.EsHistoricalPerformance15Minutes;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types.EsHistoricalPerformance24Hours;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types.EsHistoricalPerformanceLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrowaveHistoricalPerformanceWriterService {

    private static final Logger LOG = LoggerFactory.getLogger(MicrowaveHistoricalPerformanceWriterService.class);
    private static final String INDEX = "sdnperformance";
    private static final String MAPPING = "/elasticsearch/index/sdnperformance/sdnperformanceMapping.json";
    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStamp.getConverter();


    private HtDatabaseClientAbstract client;
    private HtDataBaseReaderAndWriter<EsHistoricalPerformance15Minutes> historicalPerformance15mRW;
    private HtDataBaseReaderAndWriter<EsHistoricalPerformance24Hours> historicalPerformance24hRW;
    private HtDataBaseReaderAndWriter<EsHistoricalPerformanceLogEntry> historicalPerformanceLogRW;

    public MicrowaveHistoricalPerformanceWriterService(HtDatabaseNode database) {

        LOG.info("Create {} start", MicrowaveHistoricalPerformanceWriterService.class);

        try {

            IndexClientBuilder clientBuilder =
                    IndexClientBuilder.getBuilder(INDEX).setMappingSettingJsonFileName(MAPPING);
            client = clientBuilder.create(database);
            clientBuilder.close();

            historicalPerformance15mRW = new HtDataBaseReaderAndWriter<>(client,
                    EsHistoricalPerformance15Minutes.ESDATATYPENAME, EsHistoricalPerformance15Minutes.class);
            historicalPerformance24hRW = new HtDataBaseReaderAndWriter<>(client,
                    EsHistoricalPerformance24Hours.ESDATATYPENAME, EsHistoricalPerformance24Hours.class);
            historicalPerformanceLogRW = new HtDataBaseReaderAndWriter<>(client,
                    EsHistoricalPerformanceLogEntry.ESDATATYPENAME, EsHistoricalPerformanceLogEntry.class);

        } catch (Exception e) {
            client = null;
            LOG.error("Can not start database client. Exception: {}", e.getMessage());
        }

        LOG.info("Create {} finished. DB Service {} started.", MicrowaveHistoricalPerformanceWriterService.class,
                client != null ? "sucessfully" : "not");
    }


    public void writePM(AllPm pm) {

        LOG.debug("Write {} pm records", pm.size());

        LOG.debug("Write 15m write to DB");
        historicalPerformance15mRW.doWrite(pm.getPm15());
        LOG.debug("Write 15m done, Write 24h write to DB");
        historicalPerformance24hRW.doWrite(pm.getPm24());
        LOG.debug("Write 24h done");

    }

    public void writePMLog(String mountpointName, String layerProtocolName, String msg) {

        LOG.debug("Write PM Log: {}", msg);
        EsHistoricalPerformanceLogEntry logEntry = new EsHistoricalPerformanceLogEntry(mountpointName,
                layerProtocolName, NETCONFTIME_CONVERTER.getTimeStamp().getValue(), msg);
        historicalPerformanceLogRW.doWrite(logEntry);
        LOG.debug("Write PM Log done");

    }


    static public boolean isAvailable(MicrowaveHistoricalPerformanceWriterService s) {

        if (s == null || s.client == null) {
            return false;
        }
        return true;
    }

}

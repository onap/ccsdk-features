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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database.EsObject;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.util.LinkIdentifyingObject;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.util.NetconfTimeStamp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.OtnHistoryDataG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class EsHistoricalPerformanceBase extends EsObject {

    private static final Logger LOG = LoggerFactory.getLogger(EsHistoricalPerformanceBase.class);
    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStamp.getConverter();

    @JsonIgnore private final String nodeName;
    @JsonIgnore private final String uuidInterface;
    @JsonIgnore private final String layerProtocolName;

    @JsonIgnore private String radioSignalId = null;  //Meaning of connection Id
    @JsonIgnore private String timeStamp = null;
    @JsonIgnore private Boolean suspectIntervalFlag = null;
    @JsonIgnore private String granularityPeriod = null;  //Representation of GranularityPeriodType
    @JsonIgnore private String scannerId = null;
    @JsonIgnore private Object performanceData = null;


    public EsHistoricalPerformanceBase(String nodeName, Lp actualInterface) {
        this.nodeName = nodeName;
        this.uuidInterface = actualInterface.getUuid().getValue();
        this.layerProtocolName = actualInterface.getLayerProtocolName().getValue();

    }

    protected <T extends OtnHistoryDataG> void set(T record) {
        if (record == null) {
            LOG.warn("PM Record: null record. Can not handle");
            return;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("PM Record: class {} '{}' ", record.getClass().getSimpleName(), record);
        }

        timeStamp = NETCONFTIME_CONVERTER.getTimeStampFromNetconf(record.getPeriodEndTime().getValue());
        suspectIntervalFlag = record.isSuspectIntervalFlag();
        granularityPeriod = getYangGranularityPeriodString( record.getGranularityPeriod() );
        scannerId = record.getHistoryDataId();

        if (record instanceof LinkIdentifyingObject) {
            radioSignalId = ((LinkIdentifyingObject) record).getSignalId();
        }

        performanceData = new EsPerformanceData(record);
        setEsId(genSpecificEsId(record.getPeriodEndTime().getValue()));
    }


    @JsonGetter("node-name")
    public String getNodeName() {
        return nodeName;
    }

    @JsonGetter("uuid-interface")
    public String getUuidInterface() {
        return uuidInterface;
    }

    @JsonGetter("layer-protocol-name")
    public String getLayerProtocolName() {
        return layerProtocolName;
    }

    @JsonGetter("radio-signal-id")
    public String getRadioSignalId() {
        return radioSignalId;
    }

    @JsonGetter("time-stamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    @JsonGetter("suspect-interval-flag")
    public Boolean getSuspect() {
        return suspectIntervalFlag;
    }

    @JsonGetter("granularity-period")
    public String getGranularityPeriod() {
        return granularityPeriod;
    }

    @JsonGetter("scanner-id")
    public String getScannerId() {
        return scannerId;
    }

    @JsonGetter("performance-data")
    public Object getData() {
        return performanceData;
    }



    //Adapt JSON Text
    //@JsonGetter("granularityPeriod")

    private static String getYangGranularityPeriodString(org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType yangGanularityPeriod) {
        switch(yangGanularityPeriod == null ? org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType.Unknown : yangGanularityPeriod) {
            case Period15Min:
                return "PERIOD_15MIN";
            case Period24Hours:
                return "PERIOD_24HOURS";
            default:
                return "PERIOD_UNKOWN";
        }
    }

    /**
     * Create a specific ES id for the current log.
     * @param time is the input.
     * @return a string with the generated ES Id
     */
    protected String genSpecificEsId(String time) {

        StringBuffer strBuf = new StringBuffer();
        strBuf.append(nodeName);
        strBuf.append("/");
        strBuf.append(uuidInterface);
        strBuf.append("/");
        strBuf.append(time == null || time.isEmpty() ? "Empty" : time);

        return strBuf.toString();
    }

    @Override
    public String toString() {
        return "EsHistoricalPerformanceBase [nodeName=" + nodeName + ", uuidInterface=" + uuidInterface
                + ", layerProtocolName=" + layerProtocolName + ", radioSignalId=" + radioSignalId + ", timeStamp="
                + timeStamp + ", suspectIntervalFlag=" + suspectIntervalFlag + ", granularityPeriod="
                + granularityPeriod + ", scannerId=" + scannerId + ", performanceData=" + performanceData + "]";
    }

}

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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.ToggleAlarmFilterable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Faultcurrent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Faultlog;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.slf4j.Logger;

@XmlRootElement(name = "ProblemNotification")
public class ProblemNotificationXml extends MwtNotificationBase implements GetEventType, ToggleAlarmFilterable {

    private static String EVENTTYPE = "ProblemNotification";

    @XmlElement(name = "problem")
    private String problem;

    @XmlElement(name = "severity")
    private InternalSeverity severity;

    public ProblemNotificationXml() {

    }

    /**
     * Generic Problem. All the parameters are of type Strings according to YANG specification.
     *
     * @param nodeName Name of mountpoint
     * @param uuId Name of Interface Pac
     * @param problemNameString Name of the problem
     * @param problemSeverityString Severitycode of the problem
     * @param counter Counter from device
     * @param internaltimeStampString Timestamp according to internal format.
     */
    public ProblemNotificationXml(String nodeName, String uuId, String problemNameString,
            InternalSeverity problemSeverityString, Integer counter, InternalDateAndTime internaltimeStampString) {
        super(nodeName, counter, internaltimeStampString, uuId);
        this.problem = problemNameString;
        this.severity = problemSeverityString;
    }

    public ProblemNotificationXml(FaultlogEntity input) {
        this(input.getNodeId(), input.getObjectId(), input.getProblem(), InternalSeverity.valueOf(input.getSeverity()),
                input.getCounter(), InternalDateAndTime.valueOf(input.getTimestamp()));
    }

    public String getProblem() {
        return problem;
    }

    public InternalSeverity getSeverity() {
        return severity;
    }

    public boolean isNotManagedAsCurrentProblem() {
        return !FaultEntityManager.isManagedAsCurrentProblem(getProblem());
    }

    public boolean isNoAlarmIndication() {
        return severity.isNoAlarmIndication();
    }

    @Override
    public String getUuidForMountpoint() {
        return genSpecificEsId();
    }

    @Override
    public boolean isCleared() {
        return !isNotManagedAsCurrentProblem() && isNoAlarmIndication();
    }


    /**
     * Create a specific ES id for the current log.
     * 
     * @return a string with the generated ES Id
     */
    @JsonIgnore
    public String genSpecificEsId() {
        return FaultEntityManager.genSpecificEsId(getNodeName(), getObjectId(), getProblem());
    }

    @JsonIgnore
    public Faultlog getFaultlog(SourceType sourceType) {
        return new FaultlogBuilder().setNodeId(getNodeName()).setCounter(Integer.parseInt(getCounter()))
                .setObjectId(getObjectId()).setProblem(getProblem()).setSourceType(sourceType)
                .setSeverity(getSeverity().toDataProviderSeverityType()).setTimestamp(new DateAndTime(getTimeStamp()))
                .build();
    }

    @JsonIgnore
    public Faultcurrent getFaultcurrent() {
        return new FaultcurrentBuilder().setNodeId(getNodeName()).setCounter(Integer.parseInt(getCounter()))
                .setObjectId(genSpecificEsId()).setProblem(getProblem())
                .setSeverity(getSeverity().toDataProviderSeverityType()).setTimestamp(new DateAndTime(getTimeStamp()))
                .build();
    }

    @Override
    public String toString() {
        return "ProblemNotificationXml [problem=" + problem + ", severity=" + severity + ", toString()="
                + super.toString() + "]";
    }


    @Override
    public String getEventType() {
        return EVENTTYPE;
    }

    /**
     * LOG the newly added problems of the interface pac
     * 
     * @param log of logger
     * @param uuid as log info
     * @param resultList with all problems
     * @param idxStart start of listing till end
     */
    public static void debugResultList(Logger log, String uuid, List<ProblemNotificationXml> resultList, int idxStart) {
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            int idx = 0;
            for (int t = idxStart; t < resultList.size(); t++) {
                sb.append(idx++);
                sb.append(":{");
                sb.append(resultList.get(t));
                sb.append('}');
            }
            log.debug("Found problems {} {}", uuid, sb.toString());
        }
    }

}

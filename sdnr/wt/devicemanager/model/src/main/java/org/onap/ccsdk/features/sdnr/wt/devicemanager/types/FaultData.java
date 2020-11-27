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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.types;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Faultlog;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;

/**
 * Fault information
 */
public class FaultData {

    private final List<Faultlog> problemList = new ArrayList<>();

    /**
     * @return
     */
    public int size() {
        return problemList.size();
    }

    /**
     * @param log
     * @param value
     * @param resultList
     * @param idxStart
     */
    public static void debugResultList(Logger log, String value, FaultData resultList, int idxStart) {
        // TODO Auto-generated method stub

    }

    /**
     * @param nodeId of device
     * @param sequenceNumber given by device
     * @param timeStamp of occurence
     * @param objectReference uuid in ONF model
     * @param problemName of object
     * @param severity of problem
     */
    public void add(NodeId nodeId, @Nullable Integer sequenceNumber, @Nullable DateAndTime timeStamp,
            @Nullable String objectReference, @Nullable String problemName, @Nullable SeverityType severity) {
        FaultlogBuilder bFaullog = new FaultlogBuilder().setNodeId(nodeId.getValue()).setCounter(sequenceNumber)
                .setTimestamp(timeStamp).setObjectId(objectReference).setProblem(problemName).setSeverity(severity);
        bFaullog.setSourceType(SourceType.Netconf);
        problemList.add(bFaullog.build());
    }

    /**
     * @param globalProblemList to add
     */
    public void addAll(FaultData globalProblemList) {
        problemList.addAll(globalProblemList.problemList);
    }

    /**
     *
     */
    public void clear() {}

    /**
     * @param idx of element to get
     * @return Faultlog the element
     */
    public Faultlog get(int idx) {
        return problemList.get(idx);
    }

    /**
     * Provide list with all problems of node
     * 
     * @return list
     */
    public List<Faultlog> getProblemList() {
        return problemList;
    }

}

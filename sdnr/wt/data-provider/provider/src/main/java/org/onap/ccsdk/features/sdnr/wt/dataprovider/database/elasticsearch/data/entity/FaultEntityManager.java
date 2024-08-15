/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.entity;

import javax.annotation.Nonnull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity.DatabaseIdGenerator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Fault;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;

@Deprecated
public class FaultEntityManager {

    /**
     * The leading indication for notification or events that are not in the currentProblem data of the ONF Coremodel
     */
    private static final String NOCURRENTPROBLEMINDICATION = "#";

    /**
     * Specific problems are not moving into current problem list
     *
     * @param problemName to be verified
     * @return true if problem is current
     */
    public static boolean isManagedAsCurrentProblem(String problemName) {
        return !problemName.startsWith(NOCURRENTPROBLEMINDICATION);
    }

    public static boolean isManagedAsCurrentProblem(Fault problem) {
        return isManagedAsCurrentProblem(problem.getProblem());
    }

    /**
     * Specific problems are not moving into current problem list
     *
     * @param fault to be verified
     * @return true if cleared indication
     */
    public static boolean isNoAlarmIndication(@Nonnull Fault fault) {
        return SeverityType.NonAlarmed.equals(fault.getSeverity());
    }

    /**
     * Create a specific ES id for the current log.
     *
     * @return a string with the generated ES Id
     */
    public static String genSpecificEsId(String nodeName, String objectId, String problemName) {

        String uuId = DatabaseIdGenerator.extractUuid(objectId);

        StringBuffer strBuf = new StringBuffer();
        strBuf.append(nodeName);
        strBuf.append("/");
        strBuf.append(uuId);
        strBuf.append("/");
        strBuf.append(problemName);
        return strBuf.toString();
    }

    /**
     * Create Es id
     *
     * @param fault used to create uuid for faultcurrent
     * @return String with Id
     */
    public static String genSpecificEsId(FaultcurrentEntity fault) {
        return genSpecificEsId(fault.getNodeId(), fault.getObjectId(), fault.getProblem());
    }
}

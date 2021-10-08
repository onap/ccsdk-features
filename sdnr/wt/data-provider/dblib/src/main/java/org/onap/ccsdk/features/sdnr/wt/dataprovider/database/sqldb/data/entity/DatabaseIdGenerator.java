/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Pmdata15mEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Pmdata24hEntity;

/**
 * Database id generator for those classes which need manual id generation by object
 * implementation for this spec: https://wiki.onap.org/display/DW/SDN-R+Database+for+Instanbul
 * @author jack
 *
 */
public class DatabaseIdGenerator {

    private static final String FAULT_TAG = "[layerProtocol=";
    private static final String FORMAT_PMDATA_ID = "%s/%s/%s";
    private static final String FORMAT_FAULTDATA_ID = "%s/%s/%s";
    private static final String FORMAT_INVENTORYDATA_ID = "%s/%s";

    private DatabaseIdGenerator(){

    }

    public static String getMaintenanceId(String nodeId) {
        return nodeId;
    }

    public static String getMaintenanceId(MaintenanceEntity object) {
        return object == null ? null : object.getNodeId();
    }

    public static String getControllerId() {
        return UUID.randomUUID().toString();
    }

    public static String getFaultcurrentId(String nodeId, String objectId, String problemName) {
        String uuId;

        if (objectId.endsWith("]") && objectId.contains(FAULT_TAG)) {
            uuId = objectId.substring(objectId.indexOf(FAULT_TAG) + FAULT_TAG.length(), objectId.length()-1);
        } else {
            uuId = objectId;
        }
        return String.format(FORMAT_FAULTDATA_ID, nodeId, uuId, problemName);
    }

    public static String getFaultcurrentId(FaultcurrentEntity object) {
        return object == null ? null : getFaultcurrentId(object.getNodeId(), object.getObjectId(), object.getProblem());
    }

    public static String getNetworkelementConnectionId(String nodeId) {
        return nodeId;
    }

    public static String getNetworkelementConnectionId(NetworkElementConnectionEntity object) {
        return object == null ? null : object.getNodeId();
    }

    public static String getPmData15mId(String nodeId, String uuidInterface, String timestamp) {
        return String.format(FORMAT_PMDATA_ID, nodeId, uuidInterface, timestamp);
    }

    public static String getPmData15mId(Pmdata15mEntity object) {
        return object == null ? null
                : getPmData15mId(object.getNodeName(), object.getUuidInterface(), object.getTimeStamp().getValue());
    }
    public static String getPmData24hId(String nodeId, String uuidInterface, String timestamp) {
        return String.format(FORMAT_PMDATA_ID, nodeId, uuidInterface, timestamp);
    }

    public static String getPmData24hId(Pmdata24hEntity object) {
        return object == null ? null
                : getPmData24hId(object.getNodeName(), object.getUuidInterface(), object.getTimeStamp().getValue());
    }

    public static String getInventoryId(InventoryEntity object) {
        return getInventoryId(object.getNodeId(),object.getUuid());
    }

    private static String getInventoryId(String nodeId, String uuid) {
        return String.format(FORMAT_INVENTORYDATA_ID, nodeId, uuid);
    }
}

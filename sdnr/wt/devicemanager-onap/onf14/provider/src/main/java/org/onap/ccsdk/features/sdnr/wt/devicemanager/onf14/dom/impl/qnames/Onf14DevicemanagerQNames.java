/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.qnames;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class Onf14DevicemanagerQNames {

    //Interface Notifications - common fields
    private static String COUNTER = "counter";
    private static String OBJECT_ID_REF = "object-id-ref";
    private static String TIMESTAMP = "timestamp";
    // Air-interface
    public static final QNameModule AIR_INTERFACE_2_0_MODULE =
            QNameModule.of(XMLNamespace.of("urn:onf:yang:air-interface-2-0"), Revision.of("2020-01-21"));
    public static final QName AIR_INTERFACE_2_0_MODULE_NS =
            QName.create(XMLNamespace.of("urn:onf:yang:air-interface-2-0"), "air-interface-2-0");
    public static final QName AIR_INTERFACE_PAC = QName.create(AIR_INTERFACE_2_0_MODULE, "air-interface-pac");
    public static final QName AIR_INTERFACE_CURRENT_PROBLEMS =
            QName.create(AIR_INTERFACE_2_0_MODULE, "air-interface-current-problems");
    public static final QName AIR_INTERFACE_CURRENT_PROBLEMS_LIST =
            QName.create(AIR_INTERFACE_2_0_MODULE, "current-problem-list");
    public static final QName AIR_INTERFACE_CURRENT_PROBLEMS_SEQ_NO =
            QName.create(AIR_INTERFACE_2_0_MODULE, "sequence-number");
    public static final QName AIR_INTERFACE_CURRENT_PROBLEMS_TIMESTAMP =
            QName.create(AIR_INTERFACE_2_0_MODULE, TIMESTAMP);
    public static final QName AIR_INTERFACE_CURRENT_PROBLEMS_PROBLEM_NAME =
            QName.create(AIR_INTERFACE_2_0_MODULE, "problem-name");
    public static final QName AIR_INTERFACE_CURRENT_PROBLEMS_PROBLEM_SEVERITY =
            QName.create(AIR_INTERFACE_2_0_MODULE, "problem-severity");

    // Historical Performance
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCES =
            QName.create(AIR_INTERFACE_2_0_MODULE, "air-interface-historical-performances");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST =
            QName.create(AIR_INTERFACE_2_0_MODULE, "historical-performance-data-list");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_GP =
            QName.create(AIR_INTERFACE_2_0_MODULE, "granularity-period");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_SIF =
            QName.create(AIR_INTERFACE_2_0_MODULE, "suspect-interval-flag");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_HDI =
            QName.create(AIR_INTERFACE_2_0_MODULE, "history-data-id");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST_PET =
            QName.create(AIR_INTERFACE_2_0_MODULE, "period-end-time");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_DATA =
            QName.create(AIR_INTERFACE_2_0_MODULE, "performance-data");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_ES = QName.create(AIR_INTERFACE_2_0_MODULE, "es");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_SES = QName.create(AIR_INTERFACE_2_0_MODULE, "ses");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_CSES =
            QName.create(AIR_INTERFACE_2_0_MODULE, "cses");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_UNAVAILABILITY =
            QName.create(AIR_INTERFACE_2_0_MODULE, "unavailability");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TX_LEVEL_MIN =
            QName.create(AIR_INTERFACE_2_0_MODULE, "tx-level-min");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TX_LEVEL_MAX =
            QName.create(AIR_INTERFACE_2_0_MODULE, "tx-level-max");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TX_LEVEL_AVG =
            QName.create(AIR_INTERFACE_2_0_MODULE, "tx-level-avg");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_RX_LEVEL_MIN =
            QName.create(AIR_INTERFACE_2_0_MODULE, "rx-level-min");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_RX_LEVEL_MAX =
            QName.create(AIR_INTERFACE_2_0_MODULE, "rx-level-max");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_RX_LEVEL_AVG =
            QName.create(AIR_INTERFACE_2_0_MODULE, "rx-level-avg");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TIME_XSTATES_LIST =
            QName.create(AIR_INTERFACE_2_0_MODULE, "time-xstates-list");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TIME_XSTATE_SEQNO =
            QName.create(AIR_INTERFACE_2_0_MODULE, "time-xstate-sequence-number");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TIME_XSTATE_TX_MODE =
            QName.create(AIR_INTERFACE_2_0_MODULE, "transmission-mode");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TIME_XSTATE_TIME =
            QName.create(AIR_INTERFACE_2_0_MODULE, "time");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_SNIR_MIN =
            QName.create(AIR_INTERFACE_2_0_MODULE, "snir-min");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_SNIR_MAX =
            QName.create(AIR_INTERFACE_2_0_MODULE, "snir-max");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_SNIR_AVG =
            QName.create(AIR_INTERFACE_2_0_MODULE, "snir-avg");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_XPD_MIN =
            QName.create(AIR_INTERFACE_2_0_MODULE, "xpd-min");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_XPD_MAX =
            QName.create(AIR_INTERFACE_2_0_MODULE, "xpd-max");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_XPD_AVG =
            QName.create(AIR_INTERFACE_2_0_MODULE, "xpd-avg");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_RF_TEMP_MIN =
            QName.create(AIR_INTERFACE_2_0_MODULE, "rf-temp-min");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_RF_TEMP_MAX =
            QName.create(AIR_INTERFACE_2_0_MODULE, "rf-temp-max");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_RF_TEMP_AVG =
            QName.create(AIR_INTERFACE_2_0_MODULE, "rf-temp-avg");

    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_DEFECT_BLOCKS_SUM =
            QName.create(AIR_INTERFACE_2_0_MODULE, "defect-blocks-sum");
    public static final QName AIR_INTERFACE_HISTORICAL_PERFORMANCE_TIME_PERIOD =
            QName.create(AIR_INTERFACE_2_0_MODULE, "time-period");


    // Creation notification
    public static final QName AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION =
            QName.create(AIR_INTERFACE_2_0_MODULE, "object-creation-notification");
    public static final QName AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_OBJECT_TYPE =
            QName.create(AIR_INTERFACE_2_0_MODULE, "object-type");
    public static final QName AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_COUNTER =
            QName.create(AIR_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_TIMESTAMP =
            QName.create(AIR_INTERFACE_2_0_MODULE, TIMESTAMP);
    public static final QName AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_OBJECT_ID_REF =
            QName.create(AIR_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    //AVC notification
    public static final QName AIR_INTERFACE_OBJECT_AVC_NOTIFICATION =
            QName.create(AIR_INTERFACE_2_0_MODULE, "attribute-value-changed-notification");
    public static final QName AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_COUNTER =
            QName.create(AIR_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_TIMESTAMP =
            QName.create(AIR_INTERFACE_2_0_MODULE, TIMESTAMP);
    public static final QName AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_OBJECT_ID_REF =
            QName.create(AIR_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    public static final QName AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_ATTRIBUTE_NAME =
            QName.create(AIR_INTERFACE_2_0_MODULE, "attribute-name");
    public static final QName AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_NEW_VALUE =
            QName.create(AIR_INTERFACE_2_0_MODULE, "new-value");
    //problem notification
    public static final QName AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION =
            QName.create(AIR_INTERFACE_2_0_MODULE, "problem-notification");
    public static final QName AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_OBJECT_ID_REF =
            QName.create(AIR_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    public static final QName AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_PROBLEM =
            QName.create(AIR_INTERFACE_2_0_MODULE, "problem");
    public static final QName AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_SEVERITY =
            QName.create(AIR_INTERFACE_2_0_MODULE, "severity");
    public static final QName AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_COUNTER =
            QName.create(AIR_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP =
            QName.create(AIR_INTERFACE_2_0_MODULE, TIMESTAMP);
    // Delete notification
    public static final QName AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION =
            QName.create(AIR_INTERFACE_2_0_MODULE, "object-deletion-notification");
    public static final QName AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION_COUNTER =
            QName.create(AIR_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION_OBJECT_ID_REF =
            QName.create(AIR_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    public static final QName AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION_TIMESTAMP =
            QName.create(AIR_INTERFACE_2_0_MODULE, TIMESTAMP);

    // Ethernet interface
    public static final QNameModule ETHERNET_CONTAINER_2_0_MODULE =
            QNameModule.of(XMLNamespace.of("urn:onf:yang:ethernet-container-2-0"), Revision.of("2020-01-21"));
    public static final QName ETHERNET_CONTAINER_2_0_NS =
            QName.create(XMLNamespace.of("urn:onf:yang:ethernet-container-2-0"), "ethernet-container-2-0");
    public static final QName ETHERNET_CONTAINER_PAC =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "ethernet-container-pac");
    public static final QName ETHERNET_CONTAINER_CURRENT_PROBLEMS =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "ethernet-container-current-problems");
    public static final QName ETHERNET_CONTAINER_CURRENT_PROBLEMS_LIST =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "current-problem-list");
    public static final QName ETHERNET_CONTAINER_CURRENT_PROBLEMS_SEQ_NO =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "sequence-number");
    public static final QName ETHERNET_CONTAINER_CURRENT_PROBLEMS_TIMESTAMP =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, TIMESTAMP);
    public static final QName ETHERNET_CONTAINER_CURRENT_PROBLEMS_PROBLEM_NAME =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "problem-name");
    public static final QName ETHERNET_CONTAINER_CURRENT_PROBLEMS_PROBLEM_SEVERITY =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "problem-severity");
    // Creation notification
    public static final QName ETHERNET_CONTAINER_OBJECT_CREATE_NOTIFICATION =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "object-creation-notification");
    public static final QName ETHERNET_CONTAINER_OBJECT_CREATE_NOTIFICATION_OBJECT_TYPE =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "object-type");
    public static final QName ETHERNET_CONTAINER_OBJECT_CREATE_NOTIFICATION_COUNTER =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, COUNTER);
    public static final QName ETHERNET_CONTAINER_OBJECT_CREATE_NOTIFICATION_TIMESTAMP =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, TIMESTAMP);
    public static final QName ETHERNET_CONTAINER_OBJECT_CREATE_NOTIFICATION_OBJECT_ID_REF =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, OBJECT_ID_REF);
    //AVC notification
    public static final QName ETHERNET_CONTAINER_OBJECT_AVC_NOTIFICATION =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "attribute-value-changed-notification");
    public static final QName ETHERNET_CONTAINER_OBJECT_AVC_NOTIFICATION_COUNTER =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, COUNTER);
    public static final QName ETHERNET_CONTAINER_OBJECT_AVC_NOTIFICATION_TIMESTAMP =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, TIMESTAMP);
    public static final QName ETHERNET_CONTAINER_OBJECT_AVC_NOTIFICATION_OBJECT_ID_REF =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, OBJECT_ID_REF);
    public static final QName ETHERNET_CONTAINER_OBJECT_AVC_NOTIFICATION_ATTRIBUTE_NAME =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "attribute-name");
    public static final QName ETHERNET_CONTAINER_OBJECT_AVC_NOTIFICATION_NEW_VALUE =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "new-value");
    //problem notification
    public static final QName ETHERNET_CONTAINER_OBJECT_PROBLEM_NOTIFICATION =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "problem-notification");
    public static final QName ETHERNET_CONTAINER_OBJECT_PROBLEM_NOTIFICATION_OBJECT_ID_REF =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, OBJECT_ID_REF);
    public static final QName ETHERNET_CONTAINER_OBJECT_PROBLEM_NOTIFICATION_PROBLEM =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "problem");
    public static final QName ETHERNET_CONTAINER_OBJECT_PROBLEM_NOTIFICATION_SEVERITY =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "severity");
    public static final QName ETHERNET_CONTAINER_OBJECT_PROBLEM_NOTIFICATION_COUNTER =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, COUNTER);
    public static final QName ETHERNET_CONTAINER_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, TIMESTAMP);
    // Delete notification
    public static final QName ETHERNET_CONTAINER_OBJECT_DELETE_NOTIFICATION =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, "object-deletion-notification");
    public static final QName ETHERNET_CONTAINER_OBJECT_DELETE_NOTIFICATION_COUNTER =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, COUNTER);
    public static final QName ETHERNET_CONTAINER_OBJECT_DELETE_NOTIFICATION_OBJECT_ID_REF =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, OBJECT_ID_REF);
    public static final QName ETHERNET_CONTAINER_OBJECT_DELETE_NOTIFICATION_TIMESTAMP =
            QName.create(ETHERNET_CONTAINER_2_0_MODULE, TIMESTAMP);

    //Wire interface
    public static final QNameModule WIRE_INTERFACE_2_0_MODULE =
            QNameModule.of(XMLNamespace.of("urn:onf:yang:wire-interface-2-0"), Revision.of("2020-01-23"));
    public static final QName WIRE_INTERFACE_2_0_NS =
            QName.create(XMLNamespace.of("urn:onf:yang:wire-interface-2-0"), "wire-interface-2-0");
    public static final QName WIRE_INTERFACE_PAC = QName.create(WIRE_INTERFACE_2_0_MODULE, "wire-interface-pac");
    public static final QName WIRE_INTERFACE_CURRENT_PROBLEMS =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "wire-interface-current-problems");
    public static final QName WIRE_INTERFACE_CURRENT_PROBLEMS_LIST =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "current-problem-list");
    public static final QName WIRE_INTERFACE_CURRENT_PROBLEMS_SEQ_NO =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "sequence-number");
    public static final QName WIRE_INTERFACE_CURRENT_PROBLEMS_TIMESTAMP =
            QName.create(WIRE_INTERFACE_2_0_MODULE, TIMESTAMP);
    public static final QName WIRE_INTERFACE_CURRENT_PROBLEMS_PROBLEM_NAME =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "problem-name");
    public static final QName WIRE_INTERFACE_CURRENT_PROBLEMS_PROBLEM_SEVERITY =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "problem-severity");
    // Creation notification
    public static final QName WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "object-creation-notification");
    public static final QName WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION_OBJECT_TYPE =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "object-type");
    public static final QName WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION_COUNTER =
            QName.create(WIRE_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION_TIMESTAMP =
            QName.create(WIRE_INTERFACE_2_0_MODULE, TIMESTAMP);
    public static final QName WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION_OBJECT_ID_REF =
            QName.create(WIRE_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    //AVC notification
    public static final QName WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "attribute-value-changed-notification");
    public static final QName WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION_COUNTER =
            QName.create(WIRE_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION_TIMESTAMP =
            QName.create(WIRE_INTERFACE_2_0_MODULE, TIMESTAMP);
    public static final QName WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION_OBJECT_ID_REF =
            QName.create(WIRE_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    public static final QName WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION_ATTRIBUTE_NAME =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "attribute-name");
    public static final QName WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION_NEW_VALUE =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "new-value");
    //problem notification
    public static final QName WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "problem-notification");
    public static final QName WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_OBJECT_ID_REF =
            QName.create(WIRE_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    public static final QName WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_PROBLEM =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "problem");
    public static final QName WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_SEVERITY =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "severity");
    public static final QName WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_COUNTER =
            QName.create(WIRE_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP =
            QName.create(WIRE_INTERFACE_2_0_MODULE, TIMESTAMP);
    // Delete notification
    public static final QName WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION =
            QName.create(WIRE_INTERFACE_2_0_MODULE, "object-deletion-notification");
    public static final QName WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION_COUNTER =
            QName.create(WIRE_INTERFACE_2_0_MODULE, COUNTER);
    public static final QName WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION_OBJECT_ID_REF =
            QName.create(WIRE_INTERFACE_2_0_MODULE, OBJECT_ID_REF);
    public static final QName WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION_TIMESTAMP =
            QName.create(WIRE_INTERFACE_2_0_MODULE, TIMESTAMP);


    private final QNameModule coreModel14Module;
    private final String namespaceRevision; //TODO generate out of coreModel14Module

    private Onf14DevicemanagerQNames(QNameModule coreModel14Module, String namespaceRevision) {
        this.coreModel14Module = coreModel14Module;
        this.namespaceRevision = namespaceRevision;

    }

}

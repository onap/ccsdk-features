package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class Onf14DevicemanagerQNames {
    public static final QNameModule CORE_MODEL_1_4_MODULE =
            QNameModule.create(XMLNamespace.of("urn:onf:yang:core-model-1-4"), Revision.of("2019-11-27"));
    public static final QName CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER =
            QName.create(CORE_MODEL_1_4_MODULE, "control-construct");
    public static final QName CORE_MODEL_CC_TOP_LEVEL_EQPT = QName.create(CORE_MODEL_1_4_MODULE, "top-level-equipment");
    public static final QName CORE_MODEL_CC_EQPT = QName.create(CORE_MODEL_1_4_MODULE, "equipment");
    public static final QName CORE_MODEL_CC_EQPT_GLOBAL_CLASS_UUID = QName.create(CORE_MODEL_1_4_MODULE, "uuid");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQUIPMENT =
            QName.create(CORE_MODEL_1_4_MODULE, "actual-equipment");
    public static final QName CORE_MODEL_CC_EQPT_CONTAINED_HOLDER =
            QName.create(CORE_MODEL_1_4_MODULE, "contained-holder");
    public static final QName CORE_MODEL_CC_EQPT_OCCUPYING_FRU = QName.create(CORE_MODEL_1_4_MODULE, "occupying-fru");
    public static final QName CORE_MODEL_CC_EQPT_MANUFACTURED_THING =
            QName.create(CORE_MODEL_1_4_MODULE, "manufactured-thing");
    public static final QName CORE_MODEL_CC_EQPT_MANUFACTURER_PROPS =
            QName.create(CORE_MODEL_1_4_MODULE, "manufacturer-properties");
    public static final QName CORE_MODEL_CC_EQPT_MANUFACTURER_NAME =
            QName.create(CORE_MODEL_1_4_MODULE, "manufacturer-name");
    public static final QName CORE_MODEL_CC_EQPT_MANUFACTURER_ID =
            QName.create(CORE_MODEL_1_4_MODULE, "manufacturer-identifier");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_INSTANCE =
            QName.create(CORE_MODEL_1_4_MODULE, "equipment-instance");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_INSTANCE_SERIAL_NUM =
            QName.create(CORE_MODEL_1_4_MODULE, "serial-number");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_INSTANCE_MANUFACTURED_DATE =
            QName.create(CORE_MODEL_1_4_MODULE, "manufactured-date");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE =
            QName.create(CORE_MODEL_1_4_MODULE, "equipment-type");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_VERSION =
            QName.create(CORE_MODEL_1_4_MODULE, "version");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_DESCRIPTION =
            QName.create(CORE_MODEL_1_4_MODULE, "description");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_MODEL_ID =
            QName.create(CORE_MODEL_1_4_MODULE, "model-identifier");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_PART_TYPE_ID =
            QName.create(CORE_MODEL_1_4_MODULE, "part-type-identifier");
    public static final QName CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_TYPE_NAME =
            QName.create(CORE_MODEL_1_4_MODULE, "type-name");


    public static final QName CORE_MODEL_CC_LTP = QName.create(CORE_MODEL_1_4_MODULE, "logical-termination-point");
    public static final QName CORE_MODEL_CC_LTP_LAYER_PROTOCOL = QName.create(CORE_MODEL_1_4_MODULE, "layer-protocol");
    public static final QName CORE_MODEL_CC_LTP_LAYER_PROTOCOL_NAME =
            QName.create(CORE_MODEL_1_4_MODULE, "layer-protocol-name");
    public static final QName CORE_MODEL_CC_LTP_LAYER_PROTOCOL_LOCAL_ID =
            QName.create(CORE_MODEL_1_4_MODULE, "local-id");
    public static final QName CORE_MODEL_CC_LTP_UUID = QName.create(CORE_MODEL_1_4_MODULE, "uuid");

    //Interface Notifications - common fields
    private static String COUNTER = "counter";
    private static String OBJECT_ID_REF = "object-id-ref";
    private static String TIMESTAMP = "timestamp";
    // Air-interface
    public static final QNameModule AIR_INTERFACE_2_0_MODULE =
            QNameModule.create(XMLNamespace.of("urn:onf:yang:air-interface-2-0"), Revision.of("2020-01-21"));
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
            QNameModule.create(XMLNamespace.of("urn:onf:yang:ethernet-container-2-0"), Revision.of("2020-01-21"));
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
            QNameModule.create(XMLNamespace.of("urn:onf:yang:wire-interface-2-0"), Revision.of("2020-01-23"));
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

}

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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

public class ORanDeviceManagerQNames {

    //ietf-system
    public static final String IETF_SYSTEM_NS = "urn:ietf:params:xml:ns:yang:ietf-system";
    public static final String IETF_SYSTEM_REVISION = "2014-08-06";
    public static final @NonNull QName IETF_SYSTEM_QNAME =
            QName.create(IETF_SYSTEM_NS, IETF_SYSTEM_REVISION, "ietf-system");
    public static final @NonNull QName IETF_SYSTEM_CONTAINER = QName.create(IETF_SYSTEM_QNAME, "system");

    //ietf-hardware.yang
    public static final String IETF_HW_NS = "urn:ietf:params:xml:ns:yang:ietf-hardware";
    public static final String IETF_HW_REVISION = "2018-03-13";
    public static final @NonNull QName IETF_HW_MODULE_NAME =
            QName.create(IETF_HW_NS, IETF_HW_REVISION, "ietf-hardware");
    public static final @NonNull QName IETF_HW_CONTAINER = QName.create(IETF_HW_MODULE_NAME, "hardware");
    public static final @NonNull QName IETF_HW_COMPONENT_LIST = QName.create(IETF_HW_MODULE_NAME, "component");
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_KEY = QName.create(IETF_HW_MODULE_NAME, "name");
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_MFG_NAME = QName.create(IETF_HW_MODULE_NAME, "mfg-name"); //leaf:String
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_UUID = QName.create(IETF_HW_MODULE_NAME, "uuid"); //leaf:yang:uuid
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_MODEL_NAME = QName.create(IETF_HW_MODULE_NAME, "model-name"); //leaf:String
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_SER_NUM = QName.create(IETF_HW_MODULE_NAME, "serial-num"); //leaf:String
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_SW_REV = QName.create(IETF_HW_MODULE_NAME, "software-rev"); //leaf:String
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_ALIAS = QName.create(IETF_HW_MODULE_NAME, "alias"); //leaf:String
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_CLASS = QName.create(IETF_HW_MODULE_NAME, "class");
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_MFG_DATE = QName.create(IETF_HW_MODULE_NAME, "mfg-date"); //leaf:yang:date-and-time
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_CONTAINS_CHILD = QName.create(IETF_HW_MODULE_NAME, "contains-child"); //leaf-list:leafref
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_PARENT = QName.create(IETF_HW_MODULE_NAME, "parent"); //leaf:leafref
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_DESC = QName.create(IETF_HW_MODULE_NAME, "description"); //leaf:String
    public static final @NonNull QName IETF_HW_COMPONENT_LIST_HW_REV = QName.create(IETF_HW_MODULE_NAME, "hardware-rev"); //leaf:String

    //odl-netconf-callhome-server.yang
    public static final String CALLHOME_SERVER_NS = "urn:opendaylight:params:xml:ns:yang:netconf-callhome-server";
    public static final String CALLHOME_SERVER_REVISION = "2020-10-15";
    public static final @NonNull QName CALLHOME_SERVER_MODULE =
            QName.create(CALLHOME_SERVER_NS, CALLHOME_SERVER_REVISION, "odl-netconf-callhome-server");
    public static final @NonNull QName CALLHOME_SERVER_CONTAINER =
            QName.create(CALLHOME_SERVER_MODULE, "netconf-callhome-server");
    public static final @NonNull QName CALLHOME_SERVER_ALLOWED_DEVICE =
            QName.create(CALLHOME_SERVER_MODULE, "allowed-devices");
    public static final @NonNull QName CALLHOME_SERVER_ALLOWED_DEVICE_DEVICE_LIST =
            QName.create(CALLHOME_SERVER_MODULE, "device");
    public static final @NonNull QName CALLHOME_SERVER_ALLOWED_DEVICE_KEY =
            QName.create(CALLHOME_SERVER_MODULE, "unique-id");

    //o-ran-hardware.yang
    public static final String ORAN_HW_NS = "urn:o-ran:hardware:1.0";
    public static final String ORAN_HW_REVISION = "2019-03-28";
    public static final @NonNull QName ORAN_HW_MODULE = QName.create(ORAN_HW_NS, ORAN_HW_REVISION, "o-ran-hardware").intern();
    public static final @NonNull QName ORAN_HW_COMPONENT = QName.create(ORAN_HW_MODULE, "O-RAN-HW-COMPONENT");

    //ietf-netconf-notifications.yang
    public static final String IETF_NETCONF_NOTIFICATIONS_NS = "urn:ietf:params:xml:ns:yang:ietf-netconf-notifications";
    public static final String IETF_NETCONF_NOTIFICATIONS_REVISION = "2012-02-06";
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_MODULE = QName.create(IETF_NETCONF_NOTIFICATIONS_NS, IETF_NETCONF_NOTIFICATIONS_REVISION, "ietf-netconf-notifications").intern();
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIG_CHANGE = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "netconf-config-change");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIRMED_COMMIT = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "netconf-confirmed-commit");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_NETCONF_SESSION_START = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "netconf-session-start");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_NETCONF_SESSION_END = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "netconf-session-end");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_NETCONF_CAPABILITY_CHANGE = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "netconf-capability-change");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_CHANGEDBY = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "changed-by");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_SERVERORUSER = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "server-or-user");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_EDITNODE = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "edit");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_USERNAME = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "username");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_SESSIONID = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "session-id");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_OPERATION = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "operation");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_TARGET = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "target");
    public static final @NonNull QName IETF_NETCONF_NOTIFICATIONS_DATASTORE = QName.create(IETF_NETCONF_NOTIFICATIONS_MODULE, "datastore");

    //o-ran-supervision.yang
    public static final String ORAN_SUPERVISION_NS = "urn:o-ran:supervision:1.0";
    public static final String ORAN_SUPERVISION_REVISION = "2022-12-05";
    public static final @NonNull QName ORAN_SUPERVISION_MODULE = QName.create(ORAN_SUPERVISION_NS, ORAN_SUPERVISION_REVISION, "o-ran-supervision");
    public static final @NonNull QName ORAN_SUPERVISION_NOTIFICATION = QName.create(ORAN_SUPERVISION_MODULE, "supervision-notification");

}

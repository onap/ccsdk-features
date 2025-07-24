/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class ORANFM extends YangModule {

    public static final String NAMESPACE = "urn:o-ran:fm:1.0";
    public static final QNameModule ORANFM_2019_02_04 =
            QNameModule.of(XMLNamespace.of(NAMESPACE), Revision.of("2019-02-04"));
    public static final QNameModule ORANFM_2022_08_15 =
            QNameModule.of(XMLNamespace.of(NAMESPACE), Revision.of("2022-08-15"));
    private static final List<QNameModule> MODULES = Arrays.asList(ORANFM_2019_02_04, ORANFM_2022_08_15);

    ORANFM(NetconfDomAccessor netconfDomAccessor, QNameModule module) {
        super(netconfDomAccessor, module);
    }

    public QName getFaultSourceQName() {
        return getQName("fault-source");
    }

    public QName getFaultIdQName() {
        return getQName("fault-id");
    }

    public QName getFaultSeverityQName() {
        return getQName("fault-severity");
    }

    public QName getFaultTextQName() {
        return getQName("fault-text");
    }

    public QName getAlarmNotifQName() {
        return getQName("alarm-notif");
    }

    public QName getFaultIsClearedQName() {
        return getQName("is-cleared");
    }

    public QName getFaultEventTimeQName() {
        return getQName("event-time");
    }

    public QName getFaultActiveAlarmListQName() {
        return getQName("active-alarm-list");
    }

    public QName getFaultActiveAlarmsQName() {
        return getQName("active-alarms");
    }

    /**
     * Get specific instance, depending on capabilities
     *
     * @param netconfDomAccessor
     * @return
     */
    public static Optional<ORANFM> getModule(NetconfDomAccessor netconfDomAccessor) {
        Capabilities capabilities = netconfDomAccessor.getCapabilites();
        for (QNameModule module : MODULES) {
            if (capabilities.isSupportingNamespaceAndRevision(module)) {
                return Optional.of(new ORANFM(netconfDomAccessor, module));
            }
        }
        return Optional.empty();
    }

}

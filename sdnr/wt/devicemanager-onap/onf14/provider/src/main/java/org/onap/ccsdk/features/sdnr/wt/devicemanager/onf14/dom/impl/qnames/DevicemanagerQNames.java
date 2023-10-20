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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.qnames;

import java.util.List;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class DevicemanagerQNames {

    protected QNameModule qNameModule;
    protected String revision;

    public DevicemanagerQNames(QNameModule qnm, String revision) {
        this.qNameModule = qnm;
        this.revision = revision;
    }

    public QName getQName(String localName) {
        return QName.create(qNameModule, localName);
    }

    public String getNamespaceRevision() {
        return revision;
    }

    public QNameModule getQNameModule() {
        return qNameModule;
    }

    public static Optional<DevicemanagerQNames> getDevicemanagerQNames(Capabilities capabilities,
            List<QNameModule> modules) {

        for (QNameModule module : modules) {

            if (capabilities.isSupportingNamespaceAndRevision(module.getNamespace().toString(),
                    module.getRevision().toString())) {
                String namespaceRevision = module.getRevision().toString();
                return Optional.of(new DevicemanagerQNames(module, namespaceRevision));
            }
        }

        return Optional.empty();
    }
}

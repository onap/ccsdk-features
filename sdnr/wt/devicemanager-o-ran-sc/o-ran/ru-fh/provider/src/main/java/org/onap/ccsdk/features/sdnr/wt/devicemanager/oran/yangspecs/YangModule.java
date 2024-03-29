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

import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class YangModule {

    protected final NetconfDomAccessor netconfDomAccessor;
    protected final QNameModule module;

    YangModule(NetconfDomAccessor netconfDomAccessor, QNameModule module) {
        super();
        this.netconfDomAccessor = netconfDomAccessor;
        this.module = module;
    }

    NetconfDomAccessor getNetconfDomAccessor() {
        return netconfDomAccessor;
    }

    public QNameModule getQNameModule() {
        return module;
    }

    public QName getQName(String localName) {
        return QName.create(module, localName);
    }

}

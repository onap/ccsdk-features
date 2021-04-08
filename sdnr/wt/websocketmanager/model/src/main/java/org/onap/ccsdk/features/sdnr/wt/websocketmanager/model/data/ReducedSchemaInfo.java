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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data;

import org.opendaylight.yangtools.yang.common.QName;

public class ReducedSchemaInfo {
    private String namespace;
    private String revision;
    private String type;

    public ReducedSchemaInfo() {}



    public ReducedSchemaInfo(QName qname) {
        this.namespace = qname.getNamespace().toString();
        this.revision = qname.getRevision().isPresent() ? qname.getRevision().get().toString() : null;
        this.type = qname.getLocalName();
    }

    public boolean equals(QName obj) {
        return this.namespace.equals(obj.getNamespace().toString()) && this.type.equals(obj.getLocalName())
                && ((this.revision == null && obj.getRevision().isEmpty())
                        || (this.revision.equals(obj.getRevision().get().toString())));
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}

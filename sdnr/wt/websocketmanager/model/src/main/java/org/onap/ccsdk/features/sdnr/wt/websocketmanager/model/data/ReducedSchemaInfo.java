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

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;

public class ReducedSchemaInfo {
    private String namespace;
    private String revision;
    private String type;

    public ReducedSchemaInfo() {}



    public ReducedSchemaInfo(QName qname) {
        this.namespace = qname.getNamespace().toString();
        Optional<Revision> orev = qname.getRevision();
        this.revision = orev.isPresent() ? orev.get().toString() : null;
        this.type = qname.getLocalName();
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof QName obj) {
            Optional<Revision> orev = obj.getRevision();
            if (this.namespace.equals(obj.getNamespace().toString()) && this.type.equals(obj.getLocalName())) {
                if (this.revision == null) {
                    return orev.isEmpty();
                } else if (orev.isEmpty()) {
                    return false;
                } else {
                    return orev.get().toString().equals(this.revision);
                }
            }
            return false;
        }
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
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

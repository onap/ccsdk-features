/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;

public class Scope {

    private String nodeId;
    private SchemaInfo schema;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public SchemaInfo getSchema() {
        return schema;
    }

    public void setSchema(SchemaInfo schema) {
        this.schema = schema;
    }

    @JsonIgnore
    public boolean isValid() {
        if (this.nodeId == null && this.schema == null) {
            return false;
        }
        if (this.nodeId == null && !this.schema.isValid()) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    public boolean matches(String nodeId, ReducedSchemaInfo reducedSchemaInfo) {
        if (this.nodeId == null) {
            return this.schema.matches(reducedSchemaInfo);
        } else if (this.schema == null) {
            return this.nodeId.equals(nodeId);
        }
        return this.nodeId.equals(nodeId) && this.schema.matches(reducedSchemaInfo);

    }

    public boolean addQname(QName qname) {
        if(this.schema==null) {
            this.schema = new SchemaInfo(qname);
            return true;
        }
        if(!this.schema.equalsNamespaceAndRevision(qname)) {
            return false;
        }
        this.schema.addNotification(qname.getLocalName());
        return true;
    }

    @Override
    public String toString() {
        return "Scope [nodeId=" + nodeId + ", schema=" + schema + "]";
    }

    public static Scope create(QName qname) {
        return create(null, qname);
    }

    public static Scope create(String nodeId, QName qname) {
        return create(nodeId, qname == null ? null : new SchemaInfo(qname));
    }

    public static Scope create(String nodeId, SchemaInfo schemaInfo) {
        Scope scope = new Scope();
        scope.setNodeId(nodeId);
        scope.setSchema(schemaInfo);
        return scope;
    }

    public static Scope create(String nodeId) {
        return create(nodeId, (SchemaInfo) null);
    }

    public static List<Scope> createList(List<QName> qnames) {
        return createList(null, qnames);
    }

    public static List<Scope> createList(String nodeId, List<QName> qnames) {
        List<Scope> scopes = new ArrayList<>();
        Optional<Scope> listElem = null;
        for (QName qname : qnames) {
            listElem = scopes.stream().filter(e -> e.schema != null && e.schema.equalsNamespaceAndRevision(qname))
                    .findFirst();
            if (listElem.isPresent()) {
                if (!listElem.get().addQname(qname)) {
                    scopes.add(Scope.create(nodeId, qname));
                }
            } else {
                scopes.add(Scope.create(nodeId, qname));
            }
        }
        return scopes;
    }



}

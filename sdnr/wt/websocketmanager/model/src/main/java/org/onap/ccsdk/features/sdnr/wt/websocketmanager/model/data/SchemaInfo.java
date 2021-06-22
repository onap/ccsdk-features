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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;

public class SchemaInfo {
    private String namespace;
    private String revision;
    private List<String> notification;

    public SchemaInfo() {}



    public SchemaInfo(QName qname) {
        this(qname.getNamespace().toString(),
                qname.getRevision().isPresent() ? qname.getRevision().get().toString() : null, new ArrayList<>());
        this.notification.add(qname.getLocalName());
    }

    public SchemaInfo(String namespace, String revision, List<String> notifications) {
        this.namespace = namespace;
        this.revision = revision;
        this.notification = notifications;
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

    public List<String> getNotification() {
        return notification;
    }

    public void setNotification(List<String> notification) {
        this.notification = notification;
    }

    /**
     * SchemaInfo Validation restrictions: namespace!=null notification=null or if notification list set, then size>0
     *
     * @return
     */
    @JsonIgnore
    public boolean isValid() {
        return this.namespace != null
                && (this.notification == null || (this.notification != null && !this.notification.isEmpty()));
    }

    /**
     * Check if schema(qname based info of notification) matches into this scope
     *
     * @param schema
     * @return
     */
    @JsonIgnore
    public boolean matches(ReducedSchemaInfo schema) {
        //if namespace is * placeholder => true
        if (this.namespace.equals("*")) {
            return true;
        }
        //if namespace does not match => false
        if (!this.namespace.equals(schema.getNamespace().toString())) {
            return false;
        }
        //if revision of scope is set and it does not match and is not '*' => false
        if (this.revision != null && (!this.revision.equals(schema.getRevision()) && !this.revision.equals("*"))) {
            return false;
        }
        //if notification of scope is set and is current notification is not in the list
        if (this.notification != null && !this.notification.contains(schema.getType())) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    public boolean equalsNamespaceAndRevision(QName qname) {
        if (this.namespace == null) {
            return false;
        }
        if (!this.namespace.equals(qname.getNamespace().toString())) {
            return false;
        }
        if (this.revision == null && qname.getRevision().isEmpty()) {
            return true;
        }
        if (this.revision != null) {
            return this.revision.equals(qname.getRevision().isEmpty() ? null : qname.getRevision().get().toString());
        }
        return false;
    }

    @JsonIgnore
    public void addNotification(String notification) {
        if (this.notification == null) {
            this.notification = new ArrayList<>();
        }
        this.notification.add(notification);
    }

    @Override
    public String toString() {
        return "SchemaInfo [namespace=" + namespace + ", revision=" + revision + ", notification=" + notification + "]";
    }


}

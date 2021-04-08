/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils;

import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.NotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.ReducedSchemaInfo;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.Scope;

public class UserScopes {

    private List<Scope> scopes;

    /**
     *
     * @param list array of Strings
     */
    public void setScopes(List<Scope> list) {
        this.scopes = list;
    }

    public boolean hasScope(NotificationOutput output) {
        return this.hasScope(output.getNodeId(), output.getType());
    }

    public boolean hasScope(ReducedSchemaInfo schema) {
        return this.hasScope(null, schema);
    }

    public boolean hasScope(String nodeId, ReducedSchemaInfo reducedSchemaInfo) {
        if (this.scopes == null)
            return false;
        for (Scope scope : this.scopes) {
            if (scope.matches(nodeId, reducedSchemaInfo)) {
                return true;
            }
        }
        return false;
    }

}

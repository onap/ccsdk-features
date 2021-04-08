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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager2.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.ReducedSchemaInfo;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.SchemaInfo;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.Scope;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.UserScopes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ProblemNotification;
import org.opendaylight.yangtools.yang.common.QName;

public class UserScopeTest {

    private static final String SCOPE1 = "problem-notification";
    private static final String SCOPE2 = "scope2";
    private static final String SCOPE3 = "scope3";
    private static final String SCOPE4 = "scope4";

    @Test
    public void test() {
        UserScopes scopes1 = new UserScopes();
        List<String> json1 = Arrays.asList(SCOPE1, SCOPE2, SCOPE3);
        scopes1.setScopes(Arrays.asList(buildScope(null, ProblemNotification.QNAME)));

        assertTrue(scopes1.hasScope(new ReducedSchemaInfo(ProblemNotification.QNAME)));
        assertFalse(scopes1.hasScope("RoadmA", new ReducedSchemaInfo(ObjectCreationNotification.QNAME)));

        assertTrue(scopes1.hasScope("RoadmA", new ReducedSchemaInfo(ProblemNotification.QNAME)));

    }


    private static final Scope buildScope(String nodeId, QName qname) {
        Scope scope = new Scope();
        scope.setNodeId(nodeId);
        scope.setSchema(new SchemaInfo(qname));
        return scope;
    }

}

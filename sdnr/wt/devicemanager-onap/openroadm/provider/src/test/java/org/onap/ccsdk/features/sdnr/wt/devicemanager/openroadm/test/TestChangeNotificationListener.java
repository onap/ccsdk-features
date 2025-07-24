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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmChangeNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.circuit.pack.features.CircuitPackComponents;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.base._1._0.rev110601.EditOperationType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfigChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.Edit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.EditBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.NodeStep;


public class TestChangeNotificationListener {

    private static final String NODEID = "node1";

    @Test
    @Ignore // TODO fix
    public void test() {

        NetconfAccessor netconfAccessor = mock(NetconfAccessor.class);
        DataProvider databaseService = mock(DataProvider.class);
        WebsocketManagerService notificationService = mock(WebsocketManagerService.class);
        OpenroadmChangeNotificationListener notifListener =
                new OpenroadmChangeNotificationListener(netconfAccessor, databaseService, notificationService);
        when(netconfAccessor.getNodeId()).thenReturn(new NodeId(NODEID));
        var target = DataObjectIdentifier.ofUnsafeSteps(
                List.of(new NodeStep<>(CircuitPackComponents.class)));

        notifListener.onNetconfConfigChange(createNotification(EditOperationType.Create, target));
        EventlogEntity event = new EventlogBuilder().setNodeId(NODEID)
                .setNewValue(String.valueOf(EditOperationType.Create)).setObjectId(target.toString()).build();
        verify(databaseService).writeEventLog(event);

    }

    /**
     * @param type
     * @return
     */
    private static NetconfConfigChange createNotification(EditOperationType type, DataObjectIdentifier<?> target) {
        NetconfConfigChange change = mock(NetconfConfigChange.class);

        @SuppressWarnings("null") final @NonNull List<Edit> edits = Arrays.asList(
                new EditBuilder().setOperation(type).setTarget(target).build());
        when(change.nonnullEdit()).thenReturn(edits);
        return change;
    }
}

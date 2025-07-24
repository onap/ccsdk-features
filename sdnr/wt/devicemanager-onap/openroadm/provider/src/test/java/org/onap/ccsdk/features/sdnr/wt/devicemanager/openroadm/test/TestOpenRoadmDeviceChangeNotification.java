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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmDeviceChangeNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.types.rev191129.RpcStatus;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.ChangeNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.CreateTechInfoNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.CreateTechInfoNotificationBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.change.notification.Edit;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.change.notification.EditBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.circuit.pack.features.circuit.pack.components.Component;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.base._1._0.rev110601.EditOperationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TestOpenRoadmDeviceChangeNotification {

    private static final String NODEID = "Roadm1";
    private NetconfAccessor netconfAccessor = mock(NetconfAccessor.class);
    private DataProvider databaseService = mock(DataProvider.class);
    WebsocketManagerService notificationService = mock(WebsocketManagerService.class);
    private OpenroadmDeviceChangeNotificationListener deviceChangeListener =
            new OpenroadmDeviceChangeNotificationListener(netconfAccessor, databaseService, notificationService);
    private static final NetconfTimeStamp ncTimeConverter = NetconfTimeStampImpl.getConverter();

    @Test
    public void testOnChangeNotification() {

        when(netconfAccessor.getNodeId()).thenReturn(new NodeId(NODEID));
        InstanceIdentifier<?> target = InstanceIdentifier.unsafeOf(
                List.of(DataObjectStep.of(Component.class)));

        deviceChangeListener.onChangeNotification(createNotification(EditOperationType.Create, target));
        EventlogEntity event =
                new EventlogBuilder().setNodeId(NODEID).setNewValue(String.valueOf(EditOperationType.Create))
                        .setObjectId(target.steps().iterator().next().type().toString()).setCounter(1)
                        .setAttributeName(target.getTargetType().getName()).setSourceType(SourceType.Netconf).build();
        verify(databaseService).writeEventLog(event);

    }

    @Test
    public void testOnCreateTechInfoNotification() {
        when(netconfAccessor.getNodeId()).thenReturn(new NodeId(NODEID));
        deviceChangeListener.onCreateTechInfoNotification(createTechInfoNotification());
        verify(databaseService).writeEventLog(any(EventlogEntity.class));
    }

    /**
     * @param type
     * @return
     */
    private static ChangeNotification createNotification(EditOperationType type, InstanceIdentifier<?> target) {
        ChangeNotification change = mock(ChangeNotification.class);

        @SuppressWarnings("null") final @NonNull List<Edit> edits = Arrays.asList(
                new EditBuilder().setOperation(type).setTarget(target.toIdentifier()).build());
        when(change.nonnullEdit()).thenReturn(edits);
        return change;
    }

    private static CreateTechInfoNotification createTechInfoNotification() {
        CreateTechInfoNotificationBuilder techInfoNotificationBuilder = new CreateTechInfoNotificationBuilder();
        techInfoNotificationBuilder.setLogFileName("shjkdjld/EHJkk").setShelfId("dsjhdukdgkzw")
                .setStatus(RpcStatus.Successful).setStatusMessage("TestSuccessful");
        return techInfoNotificationBuilder.build();

    }


}

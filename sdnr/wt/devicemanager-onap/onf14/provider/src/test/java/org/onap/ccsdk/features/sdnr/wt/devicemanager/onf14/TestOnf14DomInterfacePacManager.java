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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14DomInterfacePacManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LAYERPROTOCOLNAMETYPEAIRLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocol;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocolBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocolKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.LAYERPROTOCOLNAMETYPEWIRELAYER;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

@RunWith(MockitoJUnitRunner.class)
public class TestOnf14DomInterfacePacManager {

    private static String NODEIDSTRING = "nSky";

    @Mock
    NetconfDomAccessor netconfDomAccessor;
    @Mock
    DeviceManagerServiceProvider serviceProvider;
    @Mock
    ControlConstruct controlConstruct;
    @Mock
    BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer;
    @Mock
    FaultService faultService;
    @Mock
    WebsocketManagerService websocketService;
    @Mock
    DataProvider databaseService;

    Onf14DomInterfacePacManager domPacManager;
    NodeId nodeId = new NodeId(NODEIDSTRING);

    @Before
    public void init() {
        when(netconfDomAccessor.getNodeId()).thenReturn(nodeId);
        when(netconfDomAccessor.getBindingNormalizedNodeSerializer()).thenReturn(bindingNormalizedNodeSerializer);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getWebsocketService()).thenReturn(websocketService);
        when(serviceProvider.getDataProvider()).thenReturn(databaseService);

        domPacManager = new Onf14DomInterfacePacManager(netconfDomAccessor, serviceProvider);

        UniversalId uuid = new UniversalId("0Aabcdef-0abc-0cfD-0abC-0123456789AB");
        @NonNull Map<LogicalTerminationPointKey, LogicalTerminationPoint> lptMap = new HashMap<LogicalTerminationPointKey, LogicalTerminationPoint>();
        var lpMap = new HashMap<LayerProtocolKey, LayerProtocol>();

        LayerProtocol lp = new LayerProtocolBuilder().setLayerProtocolName(LAYERPROTOCOLNAMETYPEAIRLAYER.class).setLocalId("TESTAIRLAYER").build();
        LayerProtocolKey lpKey = new LayerProtocolKey("AIRPROTOCOL");
        lpMap.put(lpKey, lp);
        LogicalTerminationPoint ltp = new LogicalTerminationPointBuilder().setLayerProtocol(lpMap).setUuid(uuid).build();
        UniversalId ltpUuid = new UniversalId("1Aabcdef-1abc-1cfD-1abc-0123456789AB");
        LogicalTerminationPointKey ltpKey = new LogicalTerminationPointKey(ltpUuid);

        lptMap.put(ltpKey, ltp);

        lp = new LayerProtocolBuilder().setLayerProtocolName(LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER.class).setLocalId("TESTETHCONTAINERLAYER").build();
        lpKey = new LayerProtocolKey("ETHERNETCONTAINERPROTOCOL");
        lpMap = new HashMap<LayerProtocolKey, LayerProtocol>();
        lpMap.put(lpKey, lp);
        ltp = new LogicalTerminationPointBuilder().setLayerProtocol(lpMap).setUuid(uuid).build();
        ltpUuid = new UniversalId("1Aabcdef-1abc-1cfD-1abc-0123456789BC");
        ltpKey = new LogicalTerminationPointKey(ltpUuid);

        lptMap.put(ltpKey, ltp);

        lp = new LayerProtocolBuilder().setLayerProtocolName(LAYERPROTOCOLNAMETYPEWIRELAYER.class).setLocalId("TESTWIRELAYER").build();
        lpKey = new LayerProtocolKey("WIREPROTOCOL");
        lpMap = new HashMap<LayerProtocolKey, LayerProtocol>();
        lpMap.put(lpKey, lp);
        ltp = new LogicalTerminationPointBuilder().setLayerProtocol(lpMap).setUuid(uuid).build();
        ltpUuid = new UniversalId("1Aabcdef-1abc-1cfD-1abc-0123456789CD");
        ltpKey = new LogicalTerminationPointKey(ltpUuid);

        lptMap.put(ltpKey, ltp);

        when(Optional.of(controlConstruct).get().nonnullLogicalTerminationPoint()).thenReturn(lptMap);
    }

    @Test
    public void test() {
        domPacManager.readKeys(controlConstruct);
    }

    @Test
    public void testProblemNotification() {
        DOMNotification domNotif = mock(DOMNotification.class);
        ProblemNotification problemNotif = mock(ProblemNotification.class);
        when(bindingNormalizedNodeSerializer.fromNormalizedNodeNotification(domNotif.getType(), domNotif.getBody())).thenReturn(problemNotif);
        when(problemNotif.getCounter()).thenReturn(10);
        when(problemNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));
        when(problemNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-0abc-abcd-0123456789AB"));
        when(problemNotif.getProblem()).thenReturn("modulationIsDownShifted");

        domPacManager.onNotification(domNotif);
    }

    @Test
    public void testAVCNotification() {
        DOMNotification domNotif = mock(DOMNotification.class);
        AttributeValueChangedNotification attrValChangedNotif = mock(AttributeValueChangedNotification.class);
        when(bindingNormalizedNodeSerializer.fromNormalizedNodeNotification(domNotif.getType(), domNotif.getBody())).thenReturn(attrValChangedNotif);
        when(attrValChangedNotif.getAttributeName()).thenReturn("12345678-0123-2345-abcd-0123456789AB");
        when(attrValChangedNotif.getCounter()).thenReturn(20);
        when(attrValChangedNotif.getNewValue()).thenReturn("new-value");
        when(attrValChangedNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-1234-abcd-0123456789AB"));
        when(attrValChangedNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));
        domPacManager.onNotification(domNotif);
    }

    @Test
    public void testObjectDeletionNotification() {
        DOMNotification domNotif = mock(DOMNotification.class);
        ObjectDeletionNotification deletionNotif = mock(ObjectDeletionNotification.class);
        when(bindingNormalizedNodeSerializer.fromNormalizedNodeNotification(domNotif.getType(), domNotif.getBody())).thenReturn(deletionNotif);
        when(deletionNotif.getCounter()).thenReturn(20);
        when(deletionNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-1234-abcd-0123456789AB"));
        when(deletionNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));
        domPacManager.onNotification(domNotif);
    }

    @Test
    public void testObjectCreationNotification() {
        DOMNotification domNotif = mock(DOMNotification.class);
        ObjectCreationNotification creationNotif = mock(ObjectCreationNotification.class);
        when(bindingNormalizedNodeSerializer.fromNormalizedNodeNotification(domNotif.getType(), domNotif.getBody())).thenReturn(creationNotif);
        when(creationNotif.getObjectType()).thenReturn("air-interface-name");
        when(creationNotif.getCounter()).thenReturn(20);
        when(creationNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-1234-abcd-0123456789AB"));
        when(creationNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));
        domPacManager.onNotification(domNotif);
    }

}

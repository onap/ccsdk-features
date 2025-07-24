package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.Onf14DomNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class TestOnf14DomNetworkElement {

    private static String NODEIDSTRING = "nSky";
    private static final QNameModule coreModelQNM =
            QNameModule.of(XMLNamespace.of("urn:onf:yang:core-model-1-4"), Revision.of("2019-11-27"));
    private static final QNameModule alarmQNM =
            QNameModule.of(XMLNamespace.of("urn:onf:yang:alarms-1-0"), Revision.of("2022-03-02"));
    private static final YangInstanceIdentifier TOPLEVELEQUIPMENT_IID =
            YangInstanceIdentifier.builder().node(QName.create(coreModelQNM, "control-construct"))
                    .node(QName.create(coreModelQNM, "top-level-equipment")).build();
    @Mock
    NetconfDomAccessor netconfDomAccessor;
    @Mock
    NetconfBindingAccessor netconfBindingAccessor;
    @Mock
    DataProvider dataProvider;
    @Mock
    FaultService faultService;
    @Mock
    PerformanceManager pmService;
    @Mock
    DeviceManagerServiceProvider serviceProvider;
    @Mock
    WebsocketManagerService websocketManagerService;
    @Mock
    DataProvider databaseService;
    @Mock
    Capabilities capabilities;

    NodeId nodeId = new NodeId(NODEIDSTRING);

    @Before
    public void init() {

        when(netconfDomAccessor.getCapabilites()).thenReturn(capabilities);
        when(netconfDomAccessor.getNetconfDomAccessor()).thenReturn(Optional.of(netconfDomAccessor));
        when(netconfDomAccessor.getNodeId()).thenReturn(nodeId);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getPerformanceManagerService()).thenReturn(pmService);
        when(netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, TOPLEVELEQUIPMENT_IID))
                .thenReturn(Optional.empty());
        when(netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, TOPLEVELEQUIPMENT_IID))
                .thenReturn(Optional.empty());
        when(capabilities.isSupportingNamespaceAndRevision(coreModelQNM)).thenReturn(true);
        when(capabilities.isSupportingNamespaceAndRevision(alarmQNM)).thenReturn(true);
    }

    @Test
    public void testWithOldInterfaceRevisions() {
        Optional<NetworkElement> onfDomNe;
        Onf14DomNetworkElementFactory factory = new Onf14DomNetworkElementFactory();
        factory.init(serviceProvider);
        onfDomNe = factory.create(netconfDomAccessor, serviceProvider);
        assertTrue(onfDomNe.isPresent());

        onfDomNe.get().register();
        onfDomNe.get().deregister();
        onfDomNe.get().getAcessor();
        onfDomNe.get().getDeviceType();
        onfDomNe.get().warmstart();
      //  onfDomNe.get().getService(null);
        assertEquals(onfDomNe.get().getNodeId().getValue(), "nSky");
    }

    @Test
    public void testWithNewInterfaceRevisions() {
        Optional<NetworkElement> onfDomNe;
        Onf14DomNetworkElementFactory factory = new Onf14DomNetworkElementFactory();
        factory.init(serviceProvider);
        onfDomNe = factory.create(netconfDomAccessor, serviceProvider);
        assertTrue(onfDomNe.isPresent());

        onfDomNe.get().register();
        onfDomNe.get().deregister();
        onfDomNe.get().getAcessor();
        onfDomNe.get().getDeviceType();
        onfDomNe.get().warmstart();
      //  onfDomNe.get().getService(null);
        assertEquals(onfDomNe.get().getNodeId().getValue(), "nSky");
    }

}

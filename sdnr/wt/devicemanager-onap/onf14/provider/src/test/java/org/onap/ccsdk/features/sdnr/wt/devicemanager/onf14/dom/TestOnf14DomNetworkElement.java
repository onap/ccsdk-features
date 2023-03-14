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
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class TestOnf14DomNetworkElement {

    private static String NODEIDSTRING = "nSky";
    private static final YangInstanceIdentifier TOPLEVELEQUIPMENT_IID =
            YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
                    .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_TOP_LEVEL_EQPT).build();
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
        when(netconfDomAccessor.getCapabilites()
                .isSupportingNamespace(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER))
                        .thenReturn(true);
        when(netconfDomAccessor.getNetconfDomAccessor()).thenReturn(Optional.of(netconfDomAccessor));
        when(netconfDomAccessor.getNodeId()).thenReturn(nodeId);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getPerformanceManagerService()).thenReturn(pmService);
        when(netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, TOPLEVELEQUIPMENT_IID))
                .thenReturn(Optional.empty());
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
        //onfDomNe.get().getService(null);
        assertEquals(onfDomNe.get().getNodeId().getValue(), "nSky");
    }

}

package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.Onf14Configuration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.Onf14NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

@RunWith(MockitoJUnitRunner.class)
public class TestOnf14DomNetworkElement {

    private static String NODEIDSTRING = "nSky";

    @Mock
    NetconfDomAccessor netconfDomAccessor;
    @Mock
    NetconfBindingAccessor netconfBindingAccessor;
    @Mock
    DataProvider dataProvider;
    @Mock
    FaultService faultService;
    @Mock
    DeviceManagerServiceProvider serviceProvider;
    @Mock
    WebsocketManagerService websocketManagerService;
    @Mock
    DataProvider databaseService;
    @Mock
    Capabilities capabilities;
    @Mock
    BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer;

    NodeId nodeId = new NodeId(NODEIDSTRING);

    @Before
    public void init() {

        ConfigurationFileRepresentation configurationRepresentation = mock(ConfigurationFileRepresentation.class);
        when(serviceProvider.getConfigurationFileRepresentation()).thenReturn(configurationRepresentation);
        Optional<Onf14Configuration> onf14Cfg = Optional.of(new Onf14Configuration(configurationRepresentation));
        System.out.println(onf14Cfg.isPresent());
        when(onf14Cfg.get().isUseDomApiEnabled()).thenReturn(true);
        when(netconfDomAccessor.getCapabilites()).thenReturn(capabilities);
        when(netconfDomAccessor.getCapabilites().isSupportingNamespace(ControlConstruct.QNAME)).thenReturn(true);
        when(netconfDomAccessor.getNetconfDomAccessor()).thenReturn(Optional.of(netconfDomAccessor));
        when(netconfDomAccessor.getBindingNormalizedNodeSerializer()).thenReturn(bindingNormalizedNodeSerializer);
        when(netconfDomAccessor.getNodeId()).thenReturn(nodeId);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
    }

    @Test
    public void test() {
        Optional<NetworkElement> onfDomNe;

        Onf14NetworkElementFactory factory = new Onf14NetworkElementFactory();
        factory.init(serviceProvider);
        onfDomNe = factory.create(netconfDomAccessor, serviceProvider);
        assertTrue(onfDomNe.isPresent());

        onfDomNe.get().register();
        onfDomNe.get().deregister();
        onfDomNe.get().getAcessor();
        onfDomNe.get().getDeviceType();
        onfDomNe.get().warmstart();
        onfDomNe.get().getService(null);
        assertEquals(onfDomNe.get().getNodeId().getValue(), "nSky");
    }

}

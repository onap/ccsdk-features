package org.onap.ccsdk.features.sdnr.northbound.energysavings;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.energysavings.rev150105.PayloadInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.energysavings.rev150105.PayloadOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import com.sun.jersey.api.client.WebResource;

public class EnergysavingsProviderTest {

    private EnergysavingsProvider esProvider;
    private PayloadInput input;

    @Before
    public void setUp() throws Exception {
        DataBroker dataBroker = mock(DataBroker.class);
        RpcProviderRegistry rpcRegistry = mock(RpcProviderRegistry.class);
        esProvider = new EnergysavingsProvider(dataBroker, rpcRegistry);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testDmaapWebResource() {
        WebResource dmaapWebResource = esProvider.getDmaapSdnrToPolicyWebResource();
        // fail("Not yet implemented");
    }

    @Test
    public void testNullInput() {
        Future<RpcResult<PayloadOutput>> futureOutput = esProvider.payload(null);
        try {
            PayloadOutput output = futureOutput.get().getResult();
            output.getResult().equals("Input is null");
        } catch (Exception e) {
        }
    }

}

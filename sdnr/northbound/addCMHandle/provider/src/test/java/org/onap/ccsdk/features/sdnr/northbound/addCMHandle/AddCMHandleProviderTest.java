
package org.onap.ccsdk.features.sdnr.northbound.addCMHandle;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sun.jersey.api.client.WebResource;

import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class AddCMHandleProviderTest {

    private AddCMHandleProvider esProvider;

    @Before
    public void setUp() throws Exception {
        DataBroker dataBroker = mock(DataBroker.class);
        RpcProviderRegistry rpcRegistry = mock(RpcProviderRegistry.class);
        esProvider = new AddCMHandleProvider(dataBroker, rpcRegistry);
    }

    @After
    public void tearDown() throws Exception {
    }

}

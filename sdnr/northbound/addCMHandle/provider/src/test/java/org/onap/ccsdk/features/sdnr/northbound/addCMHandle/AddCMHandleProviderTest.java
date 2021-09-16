
package org.onap.ccsdk.features.sdnr.northbound.addCMHandle;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.sun.jersey.api.client.WebResource;

import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.onap.ccsdk.features.sdnr.northbound.addCMHandle.AddCMHandleProvider;
public class AddCMHandleProviderTest {

    private AddCMHandleProvider esProvider;

    @Before
    public void setUp() throws Exception {
        DataBroker dataBroker = mock(DataBroker.class);
        RpcProviderService rpcRegistry = mock(RpcProviderService.class);
        esProvider = new AddCMHandleProvider();
    }

    @After
    public void tearDown() throws Exception {
    }

}

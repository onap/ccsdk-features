package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.VesNotificationListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class NetconfNodeStateServiceMock implements NetconfNodeStateService {

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public <L extends NetconfNodeConnectListener> @NonNull ListenerRegistration<L> registerNetconfNodeConnectListener(
            @NonNull L netconfNodeConnectListener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <L extends NetconfNodeStateListener> @NonNull ListenerRegistration<L> registerNetconfNodeStateListener(
            @NonNull L netconfNodeStateListener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <L extends VesNotificationListener> @NonNull ListenerRegistration<L> registerVesNotifications(
            @NonNull L netconfNodeStateListener) {
        // TODO Auto-generated method stub
        return null;
    }

}

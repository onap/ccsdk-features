/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */

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

    }

    @Override
    public <L extends NetconfNodeConnectListener> @NonNull ListenerRegistration<L> registerNetconfNodeConnectListener(
            @NonNull L netconfNodeConnectListener) {
        return null;
    }

    @Override
    public <L extends NetconfNodeStateListener> @NonNull ListenerRegistration<L> registerNetconfNodeStateListener(
            @NonNull L netconfNodeStateListener) {
        return null;
    }

    @Override
    public <L extends VesNotificationListener> @NonNull ListenerRegistration<L> registerVesNotifications(
            @NonNull L netconfNodeStateListener) {
        return null;
    }

}

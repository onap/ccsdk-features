/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;

public interface NetconfNodeStateService extends Registration {

    /**
     * Register for indication that Master NetconNode is entering or leaving Connected state.
     * 
     * @param netconfNodeConnectListener
     * @return managing object for listener
     */
    @NonNull
    <L extends NetconfNodeConnectListener> ListenerRegistration<L> registerNetconfNodeConnectListener(
            @NonNull L netconfNodeConnectListener);

    /**
     * Register for all NetconfNode specific state changes
     * 
     * @param netconfNodeStateListener
     * @return managing object for listener
     */
    @NonNull
    <L extends NetconfNodeStateListener> ListenerRegistration<L> registerNetconfNodeStateListener(
            @NonNull L netconfNodeStateListener);

    /**
     * Register for Ves/DmaaP provided messages
     * 
     * @param netconfNodeStateListener
     * @return managing object for listener
     */
    @NonNull
    <L extends VesNotificationListener> ListenerRegistration<L> registerVesNotifications(
            @NonNull L netconfNodeStateListener);

}

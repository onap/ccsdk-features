/*
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
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;

/**
 * Interface handling netconf connection.
 */
public interface NetconfBindingAccessor extends NetconfAccessor {

    /**
     * @return the dataBroker
     */
    DataBroker getDataBroker();

    /**
     * @return the MDSAL Mountpoint service
     **/
    MountPoint getMountpoint();

    /**
     * Get handler for read/write
     * @Return Transaction
     */
    TransactionUtils getTransactionUtils();

    /**
     * Get notifications handler
     * @return
     */
    Optional<NetconfNotifications> getNotificationAccessor();

    /**
     * Register netconf notification listener for related mountpoint
     *
     * @param <T> specific child class of NotificationListener
     * @param listener listener to be called
     * @return handler to manager registration
     */
    <T extends NotificationListener> ListenerRegistration<NotificationListener> doRegisterNotificationListener(
            @NonNull T listener);

}

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

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Interface handling netconf connection.
 */
public interface NetconfDomAccessor extends NetconfAccessor {

    /**
     * @return the dataBroker
     */
    DOMDataBroker getDataBroker();

    /**
     * @return the MDSAL Mountpoint service
     **/
    DOMMountPoint getMountpoint();

    /**
     * Deliver the data into a class
     * @param <T> DataObject type
     * @param dataStoreType config or operational database
     * @param path data path
     * @return Optional<T> with object
     */
    <T extends DataObject> Optional<T> readData(LogicalDatastoreType dataStoreType, YangInstanceIdentifier path,
            Class<T> clazz);

    /**
     * Read data from device
     * @param dataStoreType
     * @param path
     * @return NormalizedNode<?, ?> with data
     */
    Optional<NormalizedNode<?, ?>> readDataNode(LogicalDatastoreType dataStoreType, YangInstanceIdentifier path);

    /**
     * Register netconf notification listener for related mountpoint
     *
     * @param <T> specific child class of DOMNotificationListener
     * @param listener listener to be called
     * @param types
     * @return handler to manager registration
     */
    <T extends DOMNotificationListener> @NonNull ListenerRegistration<DOMNotificationListener> doRegisterNotificationListener(
            @NonNull T listener, Collection<SchemaPath> types);
    /**
     * Register netconf notification listener for related mountpoint
     *
     * @param <T>
     * @param listener
     * @param types
     * @return
     */
    <T extends DOMNotificationListener> @NonNull ListenerRegistration<DOMNotificationListener> doRegisterNotificationListener(
            @NonNull T listener, SchemaPath[] types);



}

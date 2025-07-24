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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author herbert
 *
 */
public interface TransactionUtils {

    /**
     * Deliver the data back or null. Warning
     *
     * @param <T> SubType of the DataObject to be handled
     * @param dataBroker for accessing data
     * @param dataStoreType to address datastore
     * @param iid id to access data
     * @return null or object
     */
    @Nullable
    <T extends DataObject> T readData(DataBroker dataBroker, LogicalDatastoreType dataStoreType,
            InstanceIdentifier<T> iid);

    /**
     * Deliver the data back or null
     *
     * @param <T> SubType of the DataObject to be handled
     * @param dataBroker for accessing data
     * @param dataStoreType to address datastore
     * @param iid id to access data
     * @param noErrorIndication (Output) true if data could be read and are available and is not null
     * @param statusIndicator (Output) String with status indications during the read.
     * @return null or object
     */
    @Nullable
    <T extends DataObject> T readDataOptionalWithStatus(DataBroker dataBroker, LogicalDatastoreType dataStoreType,
            InstanceIdentifier<T> iid, AtomicBoolean noErrorIndication, AtomicReference<String> statusIndicator);

}

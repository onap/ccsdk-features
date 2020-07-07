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

import java.util.EventListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.AttributeChangeNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.FaultNotification;

/**
 * Indicate if device is connected or not. A NetconfNode (Mountpoint) is providing the status. If this is Master and
 * connected, this function is calles.
 */

public interface VesNotificationListener extends EventListener, AutoCloseable {

    /**
     * Called in case of fault notification
     * 
     * @param faultNotification to handle
     */
    public void onNotification(FaultNotification faultNotification);

    /**
     * Called in case of attributeChange notification
     * 
     * @param attributeChangeNotification to handle
     */
    public void onNotification(AttributeChangeNotification attributeChangeNotification);

}

/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
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
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.mdsal;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMService;

/**
 * Collecting utilities for mdsal api
 */
public class MdsalApi {
    /**
    * Get mountpoint service and throw exception if not available
    * @param <T>
    * @param mountPoint getting the service from
    * @param service class to request
    * @return service requested or throw
    * @throws IllegalStateException
    */
   public static <T extends DOMService<T, E>, E extends DOMService.Extension<T, E>> T getMountpointService(final DOMMountPoint mountPoint, final Class<T> service) {

       final var optional = mountPoint.getService(service);
       Preconditions.checkState(optional.isPresent(), "Service not present on mount point: %s", service.getName());
       return optional.get();
   }

}

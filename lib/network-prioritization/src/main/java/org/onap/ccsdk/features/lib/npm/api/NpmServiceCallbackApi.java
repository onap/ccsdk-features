/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.lib.npm.api;

import org.onap.ccsdk.features.lib.npm.NpmException;
import org.onap.ccsdk.features.lib.npm.models.NpmTransaction;

/**
 * The interface NpmServiceCallbackApi.
 * This must be implemented by all Services to receive notification back
 *
 * @author Kapil Singal
 */
public interface NpmServiceCallbackApi {

    /**
     * Process to be implemented by all services on-boarding to Npm.
     * This API will be invoked by Npm to notify Service to process a transaction
     *
     * @param npmTransaction the NpmTransaction
     *
     * @throws NpmException the npm exception
     */
    void process(NpmTransaction npmTransaction) throws NpmException;

}

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

package org.onap.ccsdk.features.lib.npm;

/**
 * The type Npm exception.
 * <p>
 * It will be thrown in following cases:
 * - Invalid Npm Transaction received
 * - Couldn't queue RECEIVED Npm Transaction
 * - Npm Transaction is already present in queue
 * - Fails to invoke Service callback API
 *
 * @author Kapil Singal
 */
public class NpmException extends Exception {

    /**
     * This is a NpmException constructor
     *
     * @param message the message
     */
    public NpmException(String message) {
        super(message);
    }

    /**
     * This is a NpmException constructor
     *
     * @param message the message
     * @param cause   the cause
     */
    public NpmException(String message, Throwable cause) {
        super(message, cause);
    }

}

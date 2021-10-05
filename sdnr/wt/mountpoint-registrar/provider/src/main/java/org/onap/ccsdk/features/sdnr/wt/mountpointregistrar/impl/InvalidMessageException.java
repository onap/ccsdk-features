/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

public class InvalidMessageException extends Exception {

    private final static String defaultMessage = "Message is invalid";
    private final String exceptionInfo;

    public InvalidMessageException() {
        this.exceptionInfo = defaultMessage;
    }

    public InvalidMessageException(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    @Override
    public String getMessage() {
        return exceptionInfo;
    }
}

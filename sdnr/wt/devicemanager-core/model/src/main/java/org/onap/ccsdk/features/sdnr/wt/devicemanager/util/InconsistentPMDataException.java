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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.util;

public class InconsistentPMDataException extends Exception {

    private static final long serialVersionUID = 1L;

    public InconsistentPMDataException() {
        super();
    }

    public InconsistentPMDataException(String message) {
        super(message);
    }

    public InconsistentPMDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InconsistentPMDataException(Throwable cause) {
        super(cause);
    }
}
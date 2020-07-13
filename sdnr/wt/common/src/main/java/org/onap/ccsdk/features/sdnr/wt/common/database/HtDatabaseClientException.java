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
package org.onap.ccsdk.features.sdnr.wt.common.database;

public class HtDatabaseClientException extends Exception {
    // constants
    private static final long serialVersionUID = 1L;
    // end of constants

    // variables
    private Throwable rootCause;
    // end of variables

    // constructors
    public HtDatabaseClientException(String message, Throwable rootCause) {
        super(message, rootCause);
        this.rootCause = rootCause;
    }
    // end of constructors

    // getters and setters
    public Throwable getRootCause() {
        return rootCause;
    }
    // end of getters and setters
}

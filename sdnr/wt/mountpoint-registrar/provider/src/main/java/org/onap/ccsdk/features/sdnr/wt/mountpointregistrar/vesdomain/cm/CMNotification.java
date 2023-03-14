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
package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmOperation;

public class CMNotification {
    private CMBasicHeaderFieldsNotification basicHeaderFields;
    private String cmNotificationId;
    private String cmSourceIndicator;
    private String cmPath;
    private String cmOperation;
    private String cmValue;

    public static CMNotificationBuilder builder() {
        return new CMNotificationBuilder();
    }

    private CMNotification(CMNotificationBuilder builder) {
        this.basicHeaderFields = builder.basicHeaderFields;
        this.cmNotificationId = builder.cmNotificationId;
        this.cmSourceIndicator = builder.cmSourceIndicator;
        this.cmPath = builder.cmPath;
        this.cmOperation = builder.cmOperation;
        this.cmValue = builder.cmValue;
    }

    public static class CMNotificationBuilder {
        private CMBasicHeaderFieldsNotification basicHeaderFields;
        private String cmNotificationId;
        private String cmSourceIndicator;
        private String cmValue;
        private String cmPath;

        private String cmOperation = CmOperation.NULL.getName();


        public CMNotification build() {
            return new CMNotification(this);
        }

        public CMNotificationBuilder withCMBasicHeaderFieldsNotification(
            CMBasicHeaderFieldsNotification basicHeaderFields) {
            this.basicHeaderFields = basicHeaderFields;
            return this;
        }

        public CMNotificationBuilder withCMNotificationId(
            String cmNotificationId) {
            this.cmNotificationId = cmNotificationId;
            return this;
        }

        public CMNotificationBuilder withCMSourceIndicator(String cmSourceIndicator) {
            this.cmSourceIndicator = cmSourceIndicator;
            return this;
        }

        public CMNotificationBuilder withCMValue(String cmValue) {
            this.cmValue = cmValue;
            return this;
        }

        public CMNotificationBuilder withCMOperation(String cmOperation) {
            this.cmOperation = cmOperation;
            return this;
        }

        public CMNotificationBuilder withCMPath(String cmPath) {
            this.cmPath = cmPath;
            return this;
        }
    }

    public CMBasicHeaderFieldsNotification getBasicHeaderFields() {
        return basicHeaderFields;
    }

    public String getCmSourceIndicator() {
        return cmSourceIndicator;
    }

    public String getCmPath() {
        return cmPath;
    }

    public String getCmNotificationId() {
        return cmNotificationId;
    }

    public String getCmOperation() {
        return cmOperation;
    }

    public String getCmValue() {
        return cmValue;
    }
}

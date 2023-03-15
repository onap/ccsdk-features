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

public class CMBasicHeaderFieldsNotification {
    private String cmNodeId;
    private String cmSequence;
    private String cmOccurrenceTime;
    private String sourceId;
    private String notificationType;

    public static CMBasicHeaderFieldsNotificationBuilder builder() {
        return new CMBasicHeaderFieldsNotificationBuilder();
    }

    private CMBasicHeaderFieldsNotification(
        CMBasicHeaderFieldsNotification.CMBasicHeaderFieldsNotificationBuilder builder) {
        this.cmNodeId = builder.cmNodeId;
        this.cmSequence = builder.cmSequence;
        this.cmOccurrenceTime = builder.cmOccurrenceTime;
        this.sourceId = builder.sourceId;
        this.notificationType = builder.notificationType;
    }

    public static class CMBasicHeaderFieldsNotificationBuilder {
        private String cmNodeId;
        private String cmSequence;
        private String cmOccurrenceTime;
        private String sourceId;
        private String notificationType;

        public CMBasicHeaderFieldsNotification build() {
            return new CMBasicHeaderFieldsNotification(this);
        }

        public CMBasicHeaderFieldsNotificationBuilder withCMNodeId(String cmNodeId) {
            this.cmNodeId = cmNodeId;
            return this;
        }

        public CMBasicHeaderFieldsNotificationBuilder withCMSequence(
            String cmSequence) {
            this.cmSequence = cmSequence;
            return this;
        }

        public CMBasicHeaderFieldsNotificationBuilder withCMOccurrenceTime(
            String cmOccurrenceTime) {
            this.cmOccurrenceTime = cmOccurrenceTime;
            return this;
        }

        public CMBasicHeaderFieldsNotificationBuilder withSourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public CMBasicHeaderFieldsNotificationBuilder withNotificationType(
            String notificationType) {
            this.notificationType = notificationType;
            return this;
        }
            }

    public String getCmNodeId() {
        return cmNodeId;
    }

    public String getCmSequence() {
        return cmSequence;
    }

    public String getCmOccurrenceTime() {
        return cmOccurrenceTime;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getSourceId() {
        return sourceId;
    }

}

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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.service;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;

public interface DeviceManagerServiceProvider {

    /** @return Get a dataprovider **/
    @NonNull
    DataProvider getDataProvider();

    /** @return Get notification service **/
    @NonNull
    NotificationService getNotificationService();

    /** @return Get service for handling fault **/
    @NonNull
    FaultService getFaultService();

    /** @return Get service for handling fault **/
    @NonNull
    EquipmentService getEquipmentService();

    /** @return Get Active and available inventory service */
    @NonNull
    AaiService getAaiService();

    /** @return MaintenanceService object for maintenance service */
    @NonNull
    MaintenanceService getMaintenanceService();

    /** @return related service instance */
    @NonNull
    PerformanceManager getPerformanceManagerService();

    /** @return Event Handling service */
    @NonNull
    EventHandlingService getEventHandlingService();

    /** @return Get configuration descriptor */
    @NonNull
    ConfigurationFileRepresentation getConfigurationFileRepresentation();

    /** @return Get VES Collector Service for publishing VES messages to the VES Collector */
    @NonNull
    VESCollectorService getVESCollectorService();
}

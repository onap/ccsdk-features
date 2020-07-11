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
/**
 * Problems generated by DeviceMonitor
 *
 * @author herbert
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl;

import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DeviceMonitorProblems {

    /**
     * Mountpoint is not connected via NETCONF with NE/Mediator = ssh connection
     */
    connectionLossOAM(InternalSeverity.Major),

    /**
     * Mountpoint is connected via Netconf to Mediator, but mediator is not responding. Connection state to NE is
     * unknown.
     */
    connectionLossMediator(InternalSeverity.Major),

    /**
     * Mountpoint is connected via Netconf to Mediator. This connection is OK, but mediator <-> NE Connection is not OK
     */
    connectionLossNeOAM(InternalSeverity.Major);

    private static final Logger LOG = LoggerFactory.getLogger(DeviceMonitorProblems.class);
    private InternalSeverity severity;

    DeviceMonitorProblems(@Nullable InternalSeverity severity) {
        if (severity != null) {
            this.severity = severity;
        }
    }

    public InternalSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(InternalSeverity severity) {
        LOG.info("Change severity for {} from {} to {}", name(), this.severity, severity);
        this.severity = severity;
    }

}


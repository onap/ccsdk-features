/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.IniConfigurationFile;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.IniConfigurationFile.ConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.BaseSubConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.ISubConfigHandler;

public class DcaeConfig extends BaseSubConfig {
    private static final String SECTION_MARKER_DCAE = "dcae";

    private static final String PROPERTY_KEY_EVENTRECEIVERURL = "dcaeUrl";
    //private static final String PROPERTY_KEY_TESTCOLLECTOR = "dcaeTestCollector";
    private static final String PROPERTY_KEY_USERCREDENTIALS = "dcaeUserCredentials";
    private static final String PROPERTY_KEY_TIMERPERIOD = "dcaeHeartbeatPeriodSeconds";

    private static final String DEFAULT_VALUE_EVENTRECEIVERURL = "off";
    @SuppressWarnings("unused")
    private static final String DEFAULT_VALUE_TESTCOLLECTOR = "no";
    private static final String DEFAULT_VALUE_USERCREDENTIALS = "admin:admin";
    private static final int DEFAULT_VALUE_TIMERPERIOD = 120;

    private static DcaeConfig dcaeConfig = null; // Singleton of configuration data

    private String eventReceiverUrl;
    private String userCredentials;
    private Integer timerPeriodSeconds;

    private DcaeConfig() {
        super();
        this.eventReceiverUrl = DEFAULT_VALUE_EVENTRECEIVERURL;
        this.userCredentials = DEFAULT_VALUE_USERCREDENTIALS;
        this.timerPeriodSeconds = DEFAULT_VALUE_TIMERPERIOD;
    }

    private DcaeConfig(IniConfigurationFile config, ISubConfigHandler configHandler) throws ConfigurationException {
        this(config, configHandler, true);
    }

    private DcaeConfig(IniConfigurationFile config, ISubConfigHandler configHandler, boolean save)
            throws ConfigurationException {

        super(config, configHandler, SECTION_MARKER_DCAE);

        this.eventReceiverUrl = this.getString(PROPERTY_KEY_EVENTRECEIVERURL, DEFAULT_VALUE_EVENTRECEIVERURL);
        this.userCredentials = this.getString(PROPERTY_KEY_USERCREDENTIALS, DEFAULT_VALUE_USERCREDENTIALS);
        this.timerPeriodSeconds = this.getInt(PROPERTY_KEY_TIMERPERIOD, DEFAULT_VALUE_TIMERPERIOD);
        if (save) {
            config.setProperty(SECTION_MARKER_DCAE + "." + PROPERTY_KEY_EVENTRECEIVERURL, this.eventReceiverUrl);
            config.setProperty(SECTION_MARKER_DCAE + "." + PROPERTY_KEY_USERCREDENTIALS, this.userCredentials);
            config.setProperty(SECTION_MARKER_DCAE + "." + PROPERTY_KEY_TIMERPERIOD, this.timerPeriodSeconds);

            this.save();
        }
    }

    /*
     * Setter
     */

    public void setEventReceiverUrl(String eventReveicerUrl) {
        this.eventReceiverUrl = eventReveicerUrl;
    }

    public void setUserCredentials(String userCredentials) {
        this.userCredentials = userCredentials;
    }



    public void setTimerPeriodSeconds(Integer timerPeriodSeconds) {
        this.timerPeriodSeconds = timerPeriodSeconds;
    }

    /*
     * Getter
     */

    public String getEventReveicerUrl() {
        return eventReceiverUrl;
    }

    public String getUserCredentials() {
        return userCredentials;
    }


    public Integer getTimerPeriodSeconds() {
        return timerPeriodSeconds;
    }

    @Override
    public String toString() {
        return "DcaeConfig [eventReceiverUrl=" + eventReceiverUrl + ", userCredentials=" + userCredentials
                + ", timerPeriodSeconds=" + timerPeriodSeconds + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (eventReceiverUrl == null ? 0 : eventReceiverUrl.hashCode());
        result = prime * result + (timerPeriodSeconds == null ? 0 : timerPeriodSeconds.hashCode());
        result = prime * result + (userCredentials == null ? 0 : userCredentials.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DcaeConfig other = (DcaeConfig) obj;
        if (eventReceiverUrl == null) {
            if (other.eventReceiverUrl != null) {
                return false;
            }
        } else if (!eventReceiverUrl.equals(other.eventReceiverUrl)) {
            return false;
        }
        if (timerPeriodSeconds == null) {
            if (other.timerPeriodSeconds != null) {
                return false;
            }
        } else if (!timerPeriodSeconds.equals(other.timerPeriodSeconds)) {
            return false;
        }
        if (userCredentials == null) {
            if (other.userCredentials != null) {
                return false;
            }
        } else if (!userCredentials.equals(other.userCredentials)) {
            return false;
        }
        return true;
    }

     /*-------------------------------------
     * static Functions
     */

    public static DcaeConfig getDefaultConfiguration() {
        return new DcaeConfig();
    }

    public static DcaeConfig getDcae(IniConfigurationFile config, ISubConfigHandler configHandler) {
        if (dcaeConfig == null) {
            try {
                dcaeConfig = new DcaeConfig(config, configHandler);
            } catch (ConfigurationException e) {
                dcaeConfig = DcaeConfig.getDefaultConfiguration();
            }
        }
        return dcaeConfig;
    }

    public static boolean isInstantiated() {
        return dcaeConfig != null;
    }

    public static DcaeConfig reload() {
        if (dcaeConfig == null) {
            return null;
        }
        DcaeConfig tmpConfig;
        try {
            tmpConfig = new DcaeConfig(dcaeConfig.getConfig(), dcaeConfig.getConfigHandler(), false);
        } catch (ConfigurationException e) {
            tmpConfig = DcaeConfig.getDefaultConfiguration();
        }
        dcaeConfig = tmpConfig;
        return dcaeConfig;
    }

    public static void clear() {
        dcaeConfig=null;
    }
}

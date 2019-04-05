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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.config;

import java.io.File;
import java.io.IOException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.IniConfigurationFile;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.IniConfigurationFile.ConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.AaiConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.DcaeConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.DmConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.EsConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.PmConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.ToggleAlarmConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.util.ConfigFileObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtDevicemanagerConfiguration {

    private static final long FILE_POLL_INTERVAL_MS = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(HtDevicemanagerConfiguration.class);

    private static final String CONFIGURATIONFILE = "etc/devicemanager.properties";
    private static final String CONFIGURATIONTESTFILE = "test.properties"; // for
    // testing

    private static HtDevicemanagerConfiguration mObj;
    private static HtDevicemanagerConfiguration mObjTest;
    private static IniConfigurationFile mConfig;
    private final ISubConfigHandler subconfigHandler = () -> mConfig.save();

    private final ConfigFileObserver fileObserver;
    private File mFile;

    private HtDevicemanagerConfiguration(String filename) {

        try {
            this.mFile = new File(filename);
            if (!this.mFile.exists()) {
                if (!this.mFile.createNewFile()) {
                    LOG.error("Can not create file {}", filename);
                }
            }
            if (mConfig == null) {
                mConfig = new IniConfigurationFile(this.mFile);
            }
            mConfig.load();

        } catch (ConfigurationException e) {
            LOG.error("Problem loading config values: {}", e.getMessage());
        } catch (IOException e) {
            LOG.error("Problem loading config file {} : {}", filename, e.getMessage());
        }

        this.fileObserver = new ConfigFileObserver(filename, FILE_POLL_INTERVAL_MS, mConfig);
        this.fileObserver.start();
    }


    public static HtDevicemanagerConfiguration getConfiguration() {
        if (mObj == null) {
            mObj = new HtDevicemanagerConfiguration(CONFIGURATIONFILE);
        }
        return mObj;
    }
    public static HtDevicemanagerConfiguration getTestConfiguration() {
        return getTestConfiguration(CONFIGURATIONTESTFILE,false);
    }

    public static HtDevicemanagerConfiguration getTestConfiguration(boolean newInstance) {
        return getTestConfiguration(CONFIGURATIONTESTFILE,newInstance);
    }
    public static HtDevicemanagerConfiguration getTestConfiguration(String filename) {
        return getTestConfiguration(filename,false);
    }
    public static HtDevicemanagerConfiguration getTestConfiguration(final String filename,boolean newInstance) {
        if (mObjTest == null || newInstance) {
            mObjTest = new HtDevicemanagerConfiguration(filename);
        }
        return mObjTest;
    }

    public IniConfigurationFile getMConfig() {
        return mConfig;
    }

    public void registerConfigChangedListener(IConfigChangedListener l) {
        this.fileObserver.registerConfigChangedListener(l);
    }

    public void unregisterConfigChangedListener(IConfigChangedListener l) {
        this.fileObserver.unregisterConfigChangedListener(l);
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.fileObserver != null) {
            this.fileObserver.interrupt();
        }
        super.finalize();
    }

    public DcaeConfig getDcae() {
        return DcaeConfig.getDcae(mConfig, this.subconfigHandler);
    }

    public AaiConfig getAai() {
        return AaiConfig.getAai(mConfig, this.subconfigHandler);
    }

    public EsConfig getEs() {
        return EsConfig.getEs(mConfig, this.subconfigHandler);
    }

    public PmConfig getPm() {
        return PmConfig.getPm(mConfig, this.subconfigHandler);
    }

    public ToggleAlarmConfig getToggleAlarm() {
        return ToggleAlarmConfig.getTa(mConfig, this.subconfigHandler);
    }

    public DmConfig getDmConfig() {
    	return DmConfig.getDmConfig(mConfig, this.subconfigHandler);
    }

    public ISubConfigHandler getSubconfigHandler() {
        return subconfigHandler;
    }

    public static void clear() {
        mObj = null;
        mObjTest = null;
        DcaeConfig.clear();
        AaiConfig.clear();
        EsConfig.clear();
        PmConfig.clear();
        ToggleAlarmConfig.clear();
        DmConfig.clear();
    }
}

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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.conf;

/**
 * @author Michael Dürre
 *
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo.Protocol;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfStateConfig implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfStateConfig.class);

    public static final String SECTION_MARKER_NCSTATE = "netconfstate";

    private static final String PROPERTY_KEY_HANDLEASYNC = "asynchandling";

    private static final Object DEFAULT_VALUE_TRUSTALLCERTS = false;



    private final ConfigurationFileRepresentation configuration;

    public NetconfStateConfig(ConfigurationFileRepresentation configuration) {

        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_NCSTATE);
        defaults();
    }


    public boolean handleAsync() {
        return configuration.getPropertyBoolean(SECTION_MARKER_NCSTATE, PROPERTY_KEY_HANDLEASYNC);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER_NCSTATE;
    }

    @Override
    public synchronized void defaults() {
        // Add default if not available  
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_NCSTATE, PROPERTY_KEY_HANDLEASYNC,
                DEFAULT_VALUE_TRUSTALLCERTS);

    }
}

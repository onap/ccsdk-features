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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util;

import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Using pom.xml property to setup URL to database in JUnit tests. Setup impom.xml: <br>
 * <properties><databaseport>49402</databaseport> </properties> in cooperation with plugin
 * <groupId>com.github.alexcojocaru</groupId> <artifactId>elasticsearch-maven-plugin</artifactId>
 * In local development test environment port 49200 is used.
 */
public class HostInfoForTest {

    private static final Logger LOG = LoggerFactory.getLogger(HostInfoForTest.class);

    // static methods
    public static HostInfo[] get() {
        int port;

        String portAsString = System.getProperty("databaseport");
        if (portAsString == null || portAsString.isEmpty())
            port = 49200;
        else
            port = Integer.valueOf(portAsString);
        HostInfo testHost = new HostInfo("localhost", port, Protocol.HTTP);
        LOG.info("Testhost {}",testHost);
        return new HostInfo[] {testHost};
    }
    // end of static methods
}

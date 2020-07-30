/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.GeneralConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStatePublisherMain;
import org.onap.dmaap.mr.client.MRBatchingPublisher;
import org.onap.dmaap.mr.client.response.MRPublisherResponse;
import org.slf4j.Logger;

public class TestMountpointStatePublisher {

    private static final String CONFIGURATIONTESTFILE = "test3.properties";
    public Thread publisher;
    MountpointStatePublisherMain mountpointStatePublisher;
    ConfigurationFileRepresentation configFileRepresentation;
    GeneralConfig cfg;

    @Before
    public void testMountpointStatePublisherData() {
        String testJsonData =
                "{\"NodeId\":\"69322972e178_50001\",\"NetConfNodeState\":\"Connecting\",\"TimeStamp\":\"2019-11-12T12:45:08.604Z\"}";
        configFileRepresentation =
                new ConfigurationFileRepresentation(CONFIGURATIONTESTFILE);
        cfg = new GeneralConfig(configFileRepresentation);
        JSONObject jsonObj = new JSONObject(testJsonData);
        mountpointStatePublisher = new MountpointStatePublisherMain(cfg);
        mountpointStatePublisher.getStateObjects().add(jsonObj);
    }

    @Test
    public void testMountpointStatePublisherConfiguration() throws InterruptedException {
        ConfigurationFileRepresentation configFileRepresentation =
                new ConfigurationFileRepresentation(CONFIGURATIONTESTFILE);
        GeneralConfig cfg = new GeneralConfig(configFileRepresentation);

        MountpointStatePublisherMain pub = new MountpointStatePublisherMock(cfg);
        pub.createPublisher(null);
        pub.publishMessage(pub.createPublisher(null), "Test DMaaP Message");

    }

    public class MountpointStatePublisherMock extends MountpointStatePublisherMain {

        public MountpointStatePublisherMock(Configuration config) {
            super(config);
        }

        @Override
        public MRBatchingPublisher createPublisher(Properties publisherProperties) {

            return new MRBatchingPublisher() {

                @Override
                public int send(String msg) throws IOException {
                    System.out.println("Message to send - " + msg);
                    return 0;
                }

                @Override
                public int send(String partition, String msg) throws IOException {
                    return 0;
                }

                @Override
                public int send(message msg) throws IOException {
                    return 0;
                }

                @Override
                public int send(Collection<message> msgs) throws IOException {
                    return 0;
                }

                @Override
                public void close() {

                }

                @Override
                public void logTo(Logger log) {

                }

                @Override
                public void setApiCredentials(String apiKey, String apiSecret) {

                }

                @Override
                public void clearApiCredentials() {

                }

                @Override
                public int getPendingMessageCount() {
                    return 0;
                }

                @Override
                public List<message> close(long timeout, TimeUnit timeoutUnits)
                        throws IOException, InterruptedException {
                    return null;
                }

                @Override
                public MRPublisherResponse sendBatchWithResponse() {
                    return null;
                }

            };
        }
    }

    @After
    public void after() {
        File file = new File(CONFIGURATIONTESTFILE);
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }

    }

}

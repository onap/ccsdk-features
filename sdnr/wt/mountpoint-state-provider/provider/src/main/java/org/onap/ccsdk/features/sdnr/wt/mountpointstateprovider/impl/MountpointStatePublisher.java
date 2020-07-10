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

package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.dmaap.mr.client.MRBatchingPublisher;
import org.onap.dmaap.mr.client.MRClientFactory;
import org.onap.dmaap.mr.client.response.MRPublisherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MountpointStatePublisher implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointStatePublisher.class);
    public static final List<JSONObject> stateObjects = new LinkedList<JSONObject>();
    static MRBatchingPublisher pub;
    Properties publisherProperties = new Properties();
    static boolean closePublisher = false; //Set this to true in the "Close" method of MountpointStateProviderImpl
    private int fetchPause = 5000; // Default pause between fetch - 5 seconds


    public MountpointStatePublisher(Configuration config) {
        initialize(config);
    }

    public void initialize(Configuration config) {
        LOG.info("In initializePublisher method of MountpointStatePublisher");
        GeneralConfig generalCfg = (GeneralConfig) config;

        publisherProperties.put(GeneralConfig.PROPERTY_KEY_PUBLISHER_TRANSPORTTYPE, generalCfg.getTransportType());
        publisherProperties.put(GeneralConfig.PROPERTY_KEY_PUBLISHER_HOST_PORT, generalCfg.getHostPort());
        publisherProperties.put(GeneralConfig.PROPERTY_KEY_PUBLISHER_CONTENTTYPE, generalCfg.getContenttype());
        publisherProperties.put(GeneralConfig.PROPERTY_KEY_PUBLISHER_TOPIC, generalCfg.getTopic());
        publisherProperties.put(GeneralConfig.PROPERTY_KEY_PUBLISHER_MAXBATCHSIZE, generalCfg.getMaxBatchSize());
        publisherProperties.put(GeneralConfig.PROPERTY_KEY_PUBLISHER_MAXAGEMS, generalCfg.getMaxAgeMs());
        publisherProperties.put(GeneralConfig.PROPERTY_KEY_PUBLISHER_MESSAGESENTTHREADOCCURANCE,
                generalCfg.getMessageSentThreadOccurrence());

        createPublisher(publisherProperties);
    }

    public MRBatchingPublisher createPublisher(Properties publisherProperties) {

        try {
            pub = MRClientFactory.createBatchingPublisher(publisherProperties, false);
            return pub;
        } catch (IOException e) {
            LOG.info("Exception while creating a publisher", e);

        }
        return null;
    }

    public void publishMessage(MRBatchingPublisher pub, String msg) {
        LOG.info("Publishing message {} - ", msg);
        try {
            pub.send(msg);
        } catch (IOException e) {
            LOG.info("Exception while publishing a mesage ", e);
        }
    }

    public MRBatchingPublisher getPublisher() {
        return pub;
    }

    public void run() {

        while (!closePublisher) {
            try {
                if (stateObjects.size() > 0) {
                    JSONObject obj = ((LinkedList<JSONObject>) stateObjects).removeFirst();
                    publishMessage(getPublisher(), obj.toString());
                } else {
                    pauseThread();
                }
            } catch (Exception ex) {
                LOG.error("Exception while publishing message, ignoring and continuing ... ", ex);
            }

            MRPublisherResponse res = pub.sendBatchWithResponse(); // As per dmaap-client code understanding, this need not be called but for some reason the messages are not pushed unless this is called
            LOG.debug("Response message = {} ", res.toString());
        }
    }

    private void pauseThread() throws InterruptedException {
        if (fetchPause > 0) {
            LOG.debug("No data yet to publish.  Pausing {} ms before retry ", fetchPause);
            Thread.sleep(fetchPause);
        } else {
            LOG.debug("No data yet to publish. No fetch pause specified - retrying immediately");
        }
    }

    public static void stopPublisher() throws IOException, InterruptedException {
        closePublisher = true;
        pub.close(100, TimeUnit.MILLISECONDS); // Send any remaining messages and close
    }
}

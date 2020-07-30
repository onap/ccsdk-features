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

public class MountpointStatePublisherMain {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointStatePublisherMain.class);
    private Thread thread = null;
    private MRBatchingPublisher pub = null;
    private List<JSONObject> stateObjects = new LinkedList<JSONObject>();
    private Properties publisherProperties = new Properties();
    private boolean closePublisher = false;
    private int publishPause = 5000; // Default pause between fetch - 5 seconds

    public MountpointStatePublisherMain(Configuration config) {
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

    public void start() {
        thread = new Thread(new MountpointStatePublisher());
        thread.start();
    }

    public void stop() throws IOException, InterruptedException {
        closePublisher = true;
        getPublisher().close(100, TimeUnit.MILLISECONDS); // Send any remaining messages and close)
    }

    private void pauseThread() throws InterruptedException {
        if (publishPause > 0) {
            LOG.debug("No data yet to publish.  Pausing {} ms before retry ", publishPause);
            Thread.sleep(publishPause);
        } else {
            LOG.debug("No data yet to publish. No fetch pause specified - retrying immediately");
        }
    }

    public void addToPublish(JSONObject publishObj) {
        getStateObjects().add(publishObj);
    }

    public List<JSONObject> getStateObjects() {
        return stateObjects;
    }

    public class MountpointStatePublisher implements Runnable {

        @Override
        public void run() {
            while (!closePublisher) {
                try {
                    if (getStateObjects().size() > 0) {
                        JSONObject obj = ((LinkedList<JSONObject>) getStateObjects()).removeFirst();
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

    }

}

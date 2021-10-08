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

import java.util.LinkedList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountpointStatePublisher implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointStatePublisher.class);
    private List<JSONObject> stateObjects = new LinkedList<>();
    private boolean publish = true;
    private int publishPause = 5000; // Default pause between fetch - 5 seconds
    private VESCollectorService vesCollectorService;

    public MountpointStatePublisher(@NonNull VESCollectorService vesCollectorService) {
        this.vesCollectorService = vesCollectorService;
    }

    public void addToPublish(JSONObject publishObj) {
        getStateObjects().add(publishObj);
    }

    public synchronized List<JSONObject> getStateObjects() {
        return stateObjects;
    }

    public void stop() {
        publish = false;
    }

    private void pauseThread() throws InterruptedException {
        if (publishPause > 0) {
            LOG.debug("No data yet to publish.  Pausing {} ms before retry ", publishPause);
            Thread.sleep(publishPause);
        } else {
            LOG.debug("No data yet to publish. No publish pause specified - retrying immediately");
        }
    }


    public VESMessage createVESMessage(JSONObject msg, VESCollectorCfgService vesCfg) {
        MountpointStateVESMessageFormatter vesFormatter = new MountpointStateVESMessageFormatter(vesCfg, vesCollectorService);
        return vesFormatter.createVESMessage(msg);
    }

    @Override
    public void run() {
        while (publish) {
            try {
                if (!getStateObjects().isEmpty()) {
                    JSONObject obj = ((LinkedList<JSONObject>) getStateObjects()).removeFirst();
                    VESMessage vesMsg = createVESMessage(obj, vesCollectorService.getConfig());
                    this.vesCollectorService.publishVESMessage(vesMsg);
                } else {
                    pauseThread();
                }
            } catch (InterruptedException e) {
                LOG.error("Exception while publishing message, ignoring and continuing ... ", e);
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                LOG.error("Exception while publishing message, ignoring and continuing ... ", ex);
            }
        }
    }
}

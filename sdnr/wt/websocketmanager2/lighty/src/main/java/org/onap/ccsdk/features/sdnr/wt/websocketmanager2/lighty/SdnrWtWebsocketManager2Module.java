/*
 * ============LICENSE_START==========================================
 * Copyright (c) 2019 PANTHEON.tech s.r.o.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.websocketmanager2.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager2.WebSocketManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the sdnr-wt-websocketmanager2-provider artifact.
 */
public class SdnrWtWebsocketManager2Module extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SdnrWtWebsocketManager2Module.class);

    private WebSocketManagerProvider webSocketManagerProvider;

    @Override
    protected boolean initProcedure() {
        webSocketManagerProvider = new WebSocketManagerProvider();
        webSocketManagerProvider.init();
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        try {
            webSocketManagerProvider.close();
        } catch (Exception e) {
            LOG.error("Unable to stop WebSocket Manager Provider ({})!", webSocketManagerProvider.getClass(), e);
            return false;
        }
        return true;
    }
}

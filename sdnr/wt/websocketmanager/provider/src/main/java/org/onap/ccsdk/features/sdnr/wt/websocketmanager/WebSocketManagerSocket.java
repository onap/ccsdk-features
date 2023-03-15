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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.DOMNotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.INotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.NotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.ReducedSchemaInfo;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.ScopeRegistration;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.ScopeRegistration.DataType;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.ScopeRegistrationResponse;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.UserScopes;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketManagerSocket extends WebSocketAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketManagerSocket.class);
    public static final String MSG_KEY_DATA = "data";
    public static final DataType MSG_KEY_SCOPES = DataType.scopes;
    public static final String MSG_KEY_PARAM = "param";
    public static final String MSG_KEY_VALUE = "value";
    public static final String MSG_KEY_SCOPE = "scope";

    public static final String KEY_NODEID = "nodeId";
    public static final String KEY_EVENTTYPE = "eventType";
    private static final String REGEX_SCOPEREGISTRATION = "\"data\"[\\s]*:[\\s]*\"scopes\"";
    private static final Pattern PATTERN_SCOPEREGISTRATION =
            Pattern.compile(REGEX_SCOPEREGISTRATION, Pattern.MULTILINE);
    private static final SecureRandom RND = new SecureRandom();
    private static final long SEND_MESSAGE_TIMEOUT_MILLIS = 1500;
    private static final int QUEUE_SIZE = 100;

    private final Thread sendingSyncThread;
    private final ArrayBlockingQueue<String> messageQueue;
    private boolean closed;

    private final Runnable sendingRunner = new Runnable() {
        @Override
        public void run() {
            LOG.debug("isrunning");
            while (!closed) {
                try {

                    String message = messageQueue.poll();
                    if (message != null) {
                        WebSocketManagerSocket.this.session.getRemote().sendStringByFuture(message)
                                .get(SEND_MESSAGE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                        LOG.debug("message sent");
                    }
                } catch (ExecutionException | TimeoutException e) {
                    LOG.warn("problem pushing message: ", e);
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted!", e);
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }

                if (messageQueue.isEmpty()) {
                    trySleep(1000);
                }

            }
            LOG.debug("isstopped");

        };
    };

    private static void trySleep(int sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * list of all sessionids
     */
    private static final List<String> sessionIds = new ArrayList<>();
    /**
     * map of sessionid <=> UserScopes
     */
    private static final HashMap<String, UserScopes> userScopesList = new HashMap<>();
    /**
     * map of class.hashCode <=> class
     */
    private static final HashMap<String, WebSocketManagerSocket> clientList = new HashMap<>();

    private static final ObjectMapper mapper = new YangToolsMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    private final String myUniqueSessionId;

    private Session session = null;

    public interface EventInputCallback {
        void onMessagePushed(final String message) throws Exception;
    }

    public WebSocketManagerSocket() {
        this.myUniqueSessionId = _genSessionId();
        this.sendingSyncThread = new Thread(this.sendingRunner);
        this.messageQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

    }

    @Override
    protected void finalize() throws Throwable {
        sessionIds.remove(this.myUniqueSessionId);
    }

    private static String _genSessionId() {
        String sid = String.valueOf(RND.nextLong());
        while (sessionIds.contains(sid)) {
            sid = String.valueOf(RND.nextLong());
        }
        sessionIds.add(sid);
        return sid;
    }

    @Override
    public void onWebSocketText(String message) {
        LOG.debug("{} has sent {}", this.getRemoteAdr(), message);
        if (!this.manageClientRequest(message)) {
            this.manageClientRequest2(message);
        }
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        LOG.debug("Binary not supported");
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        this.session = sess;
        closed = false;
        this.sendingSyncThread.start();
        clientList.put(String.valueOf(this.hashCode()), this);
        LOG.debug("client connected from {}", this.getRemoteAdr());
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        clientList.remove(String.valueOf(this.hashCode()));
        this.sendingSyncThread.interrupt();
        closed = true;
        LOG.debug("client disconnected from {}", this.getRemoteAdr());
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        LOG.debug("error caused on {}: ", this.getRemoteAdr(), cause);
    }

    private String getRemoteAdr() {
        String adr = "unknown";
        try {
            adr = this.session.getRemoteAddress().toString();
        } catch (Exception e) {
            LOG.debug("error resolving adr: {}", e.getMessage());
        }
        return adr;
    }

    /**
     *
     * @param request is a json object {"data":"scopes","scopes":["scope1","scope2",...]}
     * @return if handled
     */
    private boolean manageClientRequest(String request) {
        boolean ret = false;
        final Matcher matcher = PATTERN_SCOPEREGISTRATION.matcher(request);
        if (!matcher.find()) {
            return false;
        }
        try {
            ScopeRegistration registration = mapper.readValue(request, ScopeRegistration.class);
            if (registration != null && registration.validate() && registration.isType(MSG_KEY_SCOPES)) {
                ret = true;
                String sessionId = this.getSessionId();
                UserScopes clientDto = new UserScopes();
                clientDto.setScopes(registration.getScopes());
                userScopesList.put(sessionId, clientDto);
                this.send(mapper.writeValueAsString(ScopeRegistrationResponse.success(registration.getScopes())));
            }

        } catch (JsonProcessingException e) {
            LOG.warn("problem set scope: {}", e.getMessage());
            try {
                this.send(mapper.writeValueAsString(ScopeRegistrationResponse.error(e.getMessage())));
            } catch (JsonProcessingException e1) {
                LOG.warn("problem sending error response via ws: ", e1);
            }
        }
        return ret;
    }

    /*
     * broadcast message to all your clients
     */
    private void manageClientRequest2(String request) {
        try {
            NotificationOutput notification = mapper.readValue(request, NotificationOutput.class);
            if (notification.getNodeId() != null && notification.getType() != null) {
                this.sendToAll(notification.getNodeId(), notification.getType(), request);
            }
        } catch (Exception e) {
            LOG.warn("handle ws request failed:", e);
        }
    }

    public void send(String msg) {
        try {
            LOG.trace("sending {}", msg);
            this.messageQueue.put(msg);
        } catch (InterruptedException e) {
            LOG.warn("problem putting message into sending queue: {}", e.getMessage());
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    public String getSessionId() {
        return this.myUniqueSessionId;
    }

    private void sendToAll(INotificationOutput output) {
        try {
            sendToAll(output.getNodeId(), output.getType(), mapper.writeValueAsString(output));
        } catch (JsonProcessingException e) {
            LOG.warn("problem serializing noitifcation: ", e);
        }
    }

    private void sendToAll(String nodeId, ReducedSchemaInfo reducedSchemaInfo, String notification) {
        if (clientList.size() > 0) {
            for (Map.Entry<String, WebSocketManagerSocket> entry : clientList.entrySet()) {
                WebSocketManagerSocket socket = entry.getValue();
                if (socket != null) {
                    try {
                        UserScopes clientScopes = userScopesList.get(socket.getSessionId());
                        if (clientScopes != null) {
                            if (clientScopes.hasScope(nodeId, reducedSchemaInfo)) {
                                socket.send(notification);
                            } else {
                                LOG.debug("client has not scope {}", reducedSchemaInfo);
                            }
                        } else {
                            LOG.debug("no scopes for notifications registered");
                        }
                    } catch (Exception ioe) {
                        LOG.warn(ioe.getMessage());
                    }
                } else {
                    LOG.debug("cannot broadcast. socket is null");
                }
            }
        }
    }

    public static void broadCast(INotificationOutput output) {
        if (clientList.size() > 0) {
            Set<Entry<String, WebSocketManagerSocket>> e = clientList.entrySet();
            WebSocketManagerSocket s = e.iterator().next().getValue();
            if (s != null) {
                s.sendToAll(output);
            }
        }
    }

    public static void broadCast(DOMNotificationOutput domNotificationOutput) {
        if (clientList.size() > 0) {
            Set<Entry<String, WebSocketManagerSocket>> e = clientList.entrySet();
            WebSocketManagerSocket s = e.iterator().next().getValue();
            if (s != null) {
                s.sendToAll(domNotificationOutput);
            }
        }
    }

}

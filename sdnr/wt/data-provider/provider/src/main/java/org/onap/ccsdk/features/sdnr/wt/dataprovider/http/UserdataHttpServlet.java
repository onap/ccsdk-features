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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HttpWhiteboardServletPattern({"/userdata","/userdata/*"})
@HttpWhiteboardServletName("UserdataHttpServlet")
@Component(service = Servlet.class)
public class UserdataHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(UserdataHttpServlet.class);
    private static final String REGEX = "^\\/userdata[\\/]?([a-zA-Z0-9\\.]+)?$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);
    private static final String JWT_PAYLOAD_USERNAME_PROPERTYKEY = "sub";
    private static HtUserdataManager dbUserManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();
        final Matcher matcher = PATTERN.matcher(uri);
        if (matcher.find()) {
            LOG.debug("GET found match");
            this.handleGetRequest(req, resp, matcher.groupCount() > 0 ? matcher.group(1) : null);
        } else {
            LOG.debug("no valid request");
            super.doGet(req, resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();
        final Matcher matcher = PATTERN.matcher(uri);
        if (matcher.find()) {
            LOG.debug("PUT found match");
            final String payload = getPayload(req);
            this.handlePutRequest(req, resp, payload, matcher.groupCount() > 0 ? matcher.group(1) : null);
        } else {
            LOG.debug("no valid request");
            super.doPut(req, resp);
        }
    }

    private String getPayload(HttpServletRequest req) throws IOException {
        return DataTreeHttpServlet.readPayload(req);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();
        final Matcher matcher = PATTERN.matcher(uri);
        if (matcher.find()) {
            LOG.debug("DELETE found match");
            this.handleDeleteRequest(req, resp, matcher.groupCount() > 0 ? matcher.group(1) : null);
        } else {
            LOG.debug("no valid request");
            super.doPut(req, resp);
        }
    }

    private void handleGetRequest(HttpServletRequest req, HttpServletResponse resp, String key) {
        final String username = this.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        sendJsonResponse(resp,
                key == null ? dbUserManager.getUserdata(username) : dbUserManager.getUserdata(username, key));
    }


    private void handlePutRequest(HttpServletRequest req, HttpServletResponse resp, String data, String key) {
        final String username = this.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        boolean success = key == null ? dbUserManager.setUserdata(username, data)
                : dbUserManager.setUserdata(username, key, data);
        resp.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private void handleDeleteRequest(HttpServletRequest req, HttpServletResponse resp, String key) {
        final String username = this.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        boolean success =
                key == null ? dbUserManager.removeUserdata(username) : dbUserManager.removeUserdata(username, key);
        resp.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private String getUsername(HttpServletRequest req) {
        final String authHeader = req.getHeader("Authorization");
        if (authHeader == null) {
            return null;
        }
        String username = null;
        if (authHeader.startsWith("Basic")) {
            username = BaseHTTPClient.decodeBasicAuthHeaderUsername(authHeader);
        } else if (authHeader.startsWith("Bearer")) {
            username = decodeJWTPayloadUsername(authHeader, JWT_PAYLOAD_USERNAME_PROPERTYKEY);
        }
        return username;
    }

    public static String decodeJWTPayloadUsername(String authHeader, String key) {
        String username = null;
        if (authHeader.startsWith("Bearer")) {
            authHeader = authHeader.substring(7);
        }
        String[] tmp = authHeader.split("\\.");
        if (tmp.length == 3) {
            final String decoded = new String(Base64.getDecoder().decode(tmp[1]));
            JSONObject o = new JSONObject(decoded);
            if (o.has(key)) {
                username = o.getString(key);
                if (username != null && username.contains("@")) {
                    username = username.split("@")[0];
                }
            }
        }
        return username;
    }

    private static void sendJsonResponse(HttpServletResponse resp, String userdata) {
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        try {

            resp.getOutputStream().write(userdata.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.warn("problem sending response: ", e);
        }

    }

    public void setDatabaseClient(HtUserdataManager dbMgr) {
        dbUserManager = dbMgr;
    }

}

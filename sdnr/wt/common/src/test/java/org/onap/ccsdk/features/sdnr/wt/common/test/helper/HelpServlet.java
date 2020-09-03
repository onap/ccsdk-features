/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2018 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.test.helper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.ccsdk.features.sdnr.wt.common.http.BaseServlet;

public class HelpServlet extends BaseServlet implements IPublicServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final String RESPONSE_GET = "This is the response get";
    public static final String RESPONSE_POST = "This is the response post";
    public static final String RESPONSE_PUT = "This is the response put";
    public static final String RESPONSE_DELETE = "This is the response delete";
    public static final String RESPONSE_OPTIONS = "This is the response options";
    private static final String HOST = "localhost";
    private int port;
    private boolean isoff;

    /**
     * @param port
     */
    public HelpServlet(int port) {
        this.port = port;
        this.isoff = true;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp);
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(200);
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp);
    }

    @Override
    protected String getOfflineResponse() {
        return "offline";
    }

    @Override
    protected boolean isOff() {
        return this.isoff;
    }

    @Override
    protected boolean doTrustAll() {
        return false;
    }

    @Override
    protected void trustAll(boolean trust) {

    }

    @Override
    protected String getRemoteUrl(String uri) {
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        if (uri.startsWith("base")) {
            uri = uri.substring("base".length());
        }
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        String base = "http://" + HOST + ":" + this.port;
        if (!base.endsWith("/")) {
            base += "/";
        }

        return base + uri;
    }

    @Override
    protected boolean trustInsecure() {
        return false;
    }

    @Override
    protected boolean isCorsEnabled() {
        return false;
    }

    /**
     * @param b
     */
    public void setOffline(boolean b) {
        this.isoff = b;
    }
}

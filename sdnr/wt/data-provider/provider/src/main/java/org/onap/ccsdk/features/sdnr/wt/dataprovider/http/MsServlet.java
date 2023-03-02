/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.MediatorServerDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMediatorserver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HttpWhiteboardServletPattern("/ms/*")
@HttpWhiteboardServletName("MsServlet")
@Component(service = Servlet.class)
public class MsServlet extends BaseServlet {

    /**
     *
     */
    private static Logger LOG = LoggerFactory.getLogger(MsServlet.class);
    private static final long serialVersionUID = -5361461082028405171L;
    private static final String OFFLINE_RESPONSE_MESSAGE = "MediatorServer interface is offline";
    private static boolean trustAll = false;
    private static MediatorServerDataProvider entryProvider;

    public MsServlet() {
        super();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(200);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected String getOfflineResponse() {
        return OFFLINE_RESPONSE_MESSAGE;
    }

    public void triggerReloadDatabaseEntries() {
        LOG.debug("external reload triggered");
        entryProvider.triggerReloadSync();
    }

    @Override
    protected boolean isOff() {
        return false;
    }

    @Override
    protected String getRemoteUrl(String uri) {
        String dbServerId = "0";
        if (uri == null)
            uri = "";
        if (uri.length() > 0) {
            uri = uri.substring("/ms/".length());
            int idx = uri.indexOf("/");
            dbServerId = uri.substring(0, idx);
            uri = uri.substring(idx);
        }
        LOG.debug("request for ms server with id={}", dbServerId);
        String url = this.getBaseUrl(dbServerId) + uri;
        LOG.debug("dest-url: {}", url);
        return url;
    }

    protected String getBaseUrl(String dbServerId) {
        return entryProvider.getHostUrl(dbServerId);
    }

    @Override
    protected boolean doTrustAll() {
        return trustAll;
    }

    @Override
    protected void trustAll(boolean trust) {
        trustAll = trust;
    }

    public void setDataProvider(HtDatabaseMediatorserver entryProvider2) {
        entryProvider = new MediatorServerDataProvider(entryProvider2);
    }

    @Override
    protected boolean trustInsecure() {
        return trustAll;
    }

    @Override
    protected boolean isCorsEnabled() {
        return false;
    }

    public void triggerReloadSync() {
        if(entryProvider!=null) {
            entryProvider.triggerReloadSync();
        }

    }
}

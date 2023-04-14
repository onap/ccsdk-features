/*
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK.apps.sdnr.wt.apigateway
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.apigateway;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

@HttpWhiteboardServletPattern("/tiles/*")
@HttpWhiteboardServletName("TilesServlet")
@Component(service = Servlet.class)
public class TilesServlet extends BaseServlet {

    private static final long serialVersionUID = 5946205120796162644L;
    private static final String OFFLINE_RESPONSE_MESSAGE = "Tiles interface is offline";
    private static boolean trustAll = false;

    public TilesServlet() {
        super();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(200);
    }

    @Override
    protected String getOfflineResponse() {
        return OFFLINE_RESPONSE_MESSAGE;
    }

    @Override
    protected boolean isOff() {
        return MyProperties.getInstance().isTilesOff();
    }

    @Override
    protected String getRemoteUrl(String uri) {

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        if (uri.startsWith("tiles")) {
            uri = uri.substring("tiles".length());
        }
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        String base = MyProperties.getInstance().getTilesBaseUrl();
        if (!base.endsWith("/")) {
            base += "/";
        }

        return base + uri;
    }

    @Override
    protected boolean doTrustAll() {
        return trustAll;
    }

    @Override
    protected void trustAll(boolean trust) {
        trustAll = trust;
    }
}

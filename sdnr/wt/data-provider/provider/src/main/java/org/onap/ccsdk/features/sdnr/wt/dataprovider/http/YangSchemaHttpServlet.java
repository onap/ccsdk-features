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
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.YangFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangSchemaHttpServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(YangSchemaHttpServlet.class);

    private static final String schemaCachePath = "cache/schema/";

    private final YangFileProvider fileProvider;

    public YangSchemaHttpServlet() {
        this.fileProvider = new YangFileProvider(schemaCachePath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        GetYangSchemaRequest request = null;
        try {
            request = new GetYangSchemaRequest(req);
        } catch (Exception e) {
            LOG.warn("bad request for yang-schema: {}", e);
        }
        if (request != null) {
            int len;
            LOG.debug("request for yang-schema for module {} with version {}", request.getModule(),
                    request.getVersion());
            if (request.hasVersion()) {
                boolean has = false;
                try {
                    has = this.fileProvider.hasFileOrNewerForModule(request.getModule(), request.getVersion());
                } catch (ParseException e1) {
                    LOG.warn("unable to parse revision: {}", e1);
                }
                if (has) {

                    try {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("text/plain");
                        len = this.fileProvider.writeOutput(request.getModule(), request.getVersion(),
                                resp.getOutputStream());
                        resp.setContentLength(len);

                    } catch (ParseException e) {
                        LOG.warn("unable to parse revision: {}", e);
                    } catch (IOException | IllegalStateException e) {
                        LOG.warn("unable to write out module {}@{}: {}", request.getModule(), request.getVersion(), e);
                    }
                } else {
                    try {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } catch (IOException | IllegalStateException e) {
                        LOG.warn("unable to write out 404 res not found: {}", e);
                    }
                }

            } else if (this.fileProvider.hasFileForModule(request.getModule())) {

                try {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.setContentType("text/plain");
                    len = this.fileProvider.writeOutput(request.getModule(), null, resp.getOutputStream());
                    resp.setContentLength(len);
                } catch (ParseException e) {
                    LOG.warn(e.getMessage());
                } catch (IOException | IllegalStateException e) {
                    LOG.warn("unable to write out module {}: {}", request.getModule(), e);
                }

            } else {
                try {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                } catch (IOException | IllegalStateException e) {
                    LOG.warn("unable to write out 404 res not found: {}", e);
                }
            }
        } else {
            try {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException | IllegalStateException e) {
                LOG.warn("unable to write out 400 bad request: {}", e);
            }
        }

    }

}

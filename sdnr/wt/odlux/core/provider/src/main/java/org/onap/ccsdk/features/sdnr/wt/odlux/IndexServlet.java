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
package org.onap.ccsdk.features.sdnr.wt.odlux;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HttpWhiteboardServletPattern({"/favicon.ico","/index2.html"})
@HttpWhiteboardServletName("IndexServlet")
@Component(service = Servlet.class)
public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = 3039669437157215355L;
    private static Logger LOG = LoggerFactory.getLogger(IndexServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI() != null && req.getRequestURI().contains("favicon.ico")) {
            this.sendFile(resp, "etc/favicon.ico", "image/x-icon");
        } else {

            LOG.debug("redirect to odlux/index.html");
            resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            resp.setHeader("Location", "odlux/index.html");
        }
    }

    private void sendFile(HttpServletResponse response, String filename, String mimeType) {
        File f = new File(filename);
        if (f.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(f.toPath());
                response.setContentType(mimeType);
                response.setContentLength(bytes.length);
                response.setStatus(HttpURLConnection.HTTP_OK);
                OutputStream os = response.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();
            } catch (IOException e) {
                LOG.debug("problem sending {}: {}", filename, e);
            }
        } else {
            LOG.debug("file not found: {}", filename);
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

}

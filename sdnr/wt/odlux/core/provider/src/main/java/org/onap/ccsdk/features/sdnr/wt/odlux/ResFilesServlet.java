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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.onap.ccsdk.features.sdnr.wt.odlux.model.bundles.OdluxBundleLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HttpWhiteboardServletPattern("/odlux/*")
@HttpWhiteboardServletName("ResFilesServlet")
@Component(service = Servlet.class)
public class ResFilesServlet extends HttpServlet {

    private static final long serialVersionUID = -6807215213921798293L;
    private static final Logger LOG = LoggerFactory.getLogger(ResFilesServlet.class);
    private static final String LOGO_OVERWRITE_FILENAME = "etc/logo.gif";
    private static final String LOGO_URL="/odlux/images/onapLogo.gif";

    private final IndexOdluxBundle indexBundle;

    public ResFilesServlet() {
        super();
        indexBundle = new IndexOdluxBundle();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final String fn = req.getRequestURI();
        LOG.debug("Get request with for URI: {}", fn);

        if(LOGO_URL.equals(fn)) {
            File f = new File(LOGO_OVERWRITE_FILENAME);
            if(f.exists()) {
                resp.setStatus(HttpURLConnection.HTTP_OK);
                resp.setContentType("image/gif");
                try {
                    Files.copy(f, resp.getOutputStream());
                } catch (IOException e) {
                    LOG.warn("Can not copy data", e);
                    resp.setStatus(500);
                }
                return;
            }
        }
        OdluxBundleLoader odluxBundleLoader = OdluxBundleLoaderImpl.getInstance();
        if (odluxBundleLoader != null) {
            String fileContent = odluxBundleLoader.getResourceContent(fn, indexBundle);
            if (fileContent != null) {
                //Store header info
                String mimeType = getMimeType(fn);
                byte[] byteContent = fileContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                int length = byteContent.length;

                LOG.debug("Found file in resources. Name {} mimetype {} length {}  and write to output stream", fn,
                        mimeType, length);
                resp.setContentType(mimeType);
                resp.setContentLength(length);
                resp.setStatus(HttpURLConnection.HTTP_OK);
                try (OutputStream os = resp.getOutputStream()) {
                    os.write(byteContent);
                    os.flush();
                } catch (IOException e) {
                    LOG.warn("Can not write data", e);
                    resp.setStatus(500);
                }
            } else {
                LOG.debug("File {} not found in res.", fn);
                resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            }
        } else {
            LOG.debug("BundleLoaderInstance not found. {}", fn);
            resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    public String loadFileContent(String filename) {
        return this.indexBundle.getResourceFileContent(filename);
    }

    //Provide own function that can be overloaded for test
    public String getMimeType(String fileName) {
        String t =  getServletContext().getMimeType(fileName);
        if(t.startsWith("text")) {
            t+="; charset=utf-8";
        }
        return t;
    }


}

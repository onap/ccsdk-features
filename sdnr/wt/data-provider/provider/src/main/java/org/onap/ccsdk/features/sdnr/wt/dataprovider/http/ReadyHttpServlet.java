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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.karaf.bundle.core.BundleInfo;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.bundle.core.BundleState;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about.MarkdownTable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyHttpServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ReadyHttpServlet.class);
    private static boolean status;


    private BundleService bundleService = null;

    public void setBundleService(BundleService bundleService) {
        this.bundleService  = bundleService;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (isReady() && this.getBundleStatesReady()) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {

            try {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException | IllegalStateException e) {
                LOG.warn("unable to write out 404 res not found: {}", e);
            }
        }
    }

    private static boolean isReady() {
        return status;
    }

    public static void setStatus(boolean s) {
        status = s;
        LOG.info("status is set to ready: {}", status);
    }

    private boolean getBundleStatesReady() {
        Bundle thisbundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext context = thisbundle ==null?null:thisbundle.getBundleContext();
        if (context == null) {
            LOG.debug("no bundle context available");
            return true;
        }
        Bundle[] bundles = context.getBundles();
        if (bundles == null || bundles.length <= 0) {
            LOG.debug("no bundles found");
            return true;
        }
        LOG.debug("found {} bundles", bundles.length);
        MarkdownTable table = new MarkdownTable();
        table.setHeader(new String[] {"Bundle-Id","Version","Symbolic-Name","Status"});
        int cntNotActive=0;

        for (Bundle bundle : bundles) {
            if(this.bundleService!=null) {
                BundleInfo info = this.bundleService.getInfo(bundle);
                if(info.getState()==BundleState.Active ) {
                    continue;
                }
                if(info.getState()==BundleState.Resolved ) {
                    if(!this.isBundleImportant(bundle.getSymbolicName())) {
                        LOG.trace("ignore not important bundle {} with state {}",bundle.getSymbolicName(),info.getState());
                        continue;
                    }
                }

                LOG.trace("bundle {} is in state {}",bundle.getSymbolicName(),info.getState());
            }
            else {
                LOG.warn("bundle service is null");
            }
            cntNotActive++;
        }

        return cntNotActive==0;
    }

    private boolean isBundleImportant(String symbolicName) {
        symbolicName = symbolicName.toLowerCase();
        if(symbolicName.contains("mdsal")) {
            return true;
        }
        if(symbolicName.contains("netconf")) {
            return true;
        }
        if(symbolicName.contains("ccsdk")) {
            return true;
        }
        if(symbolicName.contains("devicemanager")) {
            return true;
        }
        if(symbolicName.contains("restconf")) {
            return true;
        }

        return false;
    }

}

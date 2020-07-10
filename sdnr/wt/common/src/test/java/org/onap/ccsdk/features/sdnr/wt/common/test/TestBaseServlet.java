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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.test.helper.HelpServlet;
import org.onap.ccsdk.features.sdnr.wt.common.test.helper.HelpServletBase;

/**
 * @author Michael Dürre
 *
 */
public class TestBaseServlet extends HelpServletBase {

    private static final int PORT = 40003;

    public TestBaseServlet() {
        super("/base", PORT);
    }

    @Test
    public void test() throws ServletException, IOException {

        String query = "{\"query\":{\"match_all\":{}}}";
        HelpServlet servlet = new HelpServlet(PORT);
        this.setServlet(servlet);
        // test disabled message
        String expectedResponse = "offline";
        testrequest(HTTPMETHOD_GET, query, expectedResponse, false);
        testrequest(HTTPMETHOD_POST, query, expectedResponse, false);
        testrequest(HTTPMETHOD_PUT, query, expectedResponse, false);
        testrequest(HTTPMETHOD_DELETE, query, expectedResponse, false);
        servlet.setOffline(false);
        // initEsTestWebserver(port);
        testrequest(HTTPMETHOD_GET, query, HelpServletBase.RESPONSE_GET, true);
        testrequest(HTTPMETHOD_POST, query, HelpServletBase.RESPONSE_POST, true);
        testrequest(HTTPMETHOD_PUT, query, HelpServletBase.RESPONSE_PUT, true);
        testrequest(HTTPMETHOD_DELETE, query, HelpServletBase.RESPONSE_DELETE, true);
        testrequest(HTTPMETHOD_OPTIONS, query, "", false);
        // stopTestWebserver();
    }

    @Before
    public void init() throws IOException {
        HelpServletBase.initEsTestWebserver(PORT);
    }

    @After
    public void deinit() {
        HelpServletBase.stopTestWebserver();
    }
}

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
package org.onap.ccsdk.features.sdnr.wt.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Michael Dürre
 *
 *         class to get access to internal jar resources
 */
public class Resources {

    // constants
    private static final Logger LOG = LoggerFactory.getLogger(Resources.class);
    // end of constants

    // static methods
    private static URL getFileURL(Class<?> cls, String resFile) {
        Bundle b = FrameworkUtil.getBundle(cls);
        URL u;
        LOG.debug("try to get file {}", resFile);
        if (b == null) {
            LOG.info("Load resource as file: {}", resFile);
            u = getUrlForRessource(cls, resFile);
        } else {
            LOG.info("Load resource from bundle: {}", resFile);
            u = b.getEntry(resFile);
        }
        return u;
    }

    private static String readFile(final URL u) throws IOException {
        return readFile(u.openStream());
    }

    private static String readFile(final InputStream s) throws IOException {
        // read file
        final String LR = "\n";
        BufferedReader in = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine).append(LR);
        }
        in.close();
        s.close();
        return sb.toString();
    }

    public static String getFileContent(Class<?> cls, String resFile) {
        LOG.debug("loading file {} from res", resFile);
        URL u = getFileURL(cls, resFile);
        String s = null;
        if (u == null) {
            LOG.warn("cannot find resfile: {}", resFile);
            return null;
        }
        try {
            s = readFile(u);
        } catch (Exception e) {
            LOG.warn("problem reading file: {}", e.getMessage());
        }
        return s;
    }

    public static URL getUrlForRessource(Class<?> cls, String fileOrDirectory) {
        //ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = cls.getClassLoader();
        URL url = loader.getResource(fileOrDirectory);
        if (url == null && fileOrDirectory.startsWith("/")) {
            url = loader.getResource(fileOrDirectory.substring(1));
        }
        return url;
    }
    // end of static methods
}

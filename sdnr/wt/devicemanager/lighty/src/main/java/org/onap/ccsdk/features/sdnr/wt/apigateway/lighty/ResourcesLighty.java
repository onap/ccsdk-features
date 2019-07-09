/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.apigateway.lighty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THIS CLASS IS A COPY OF {@link org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.ResourcesImpl}
 * WITH REMOVED OSGi DEPENDENCIES
 */
public class ResourcesLighty implements Resources {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesLighty.class);

    private static final String RESSOURCEROOT = "src/main/resources";

    private URL getFileURL(String resFile) {
        LOG.info("Load resource as file: {}", resFile);
        return getUrlForRessource(resFile);
    }

    private File getFile(String resFile) {
        LOG.debug("try to get file {}", resFile);
        return new File(RESSOURCEROOT + resFile);
    }

    private String readFile(final URL u) throws IOException {
        return readFile(u.openStream());
    }

    private String readFile(final InputStream s) throws IOException {
        // read file
        BufferedReader in = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();
        s.close();
        return sb.toString();
    }

    @Override
    public List<URL> getFileURLs(String folder, final String filter, final boolean recursive)
            throws IOException {
        List<URL> list = new ArrayList<>();

        FileFilter ff = pathname -> {
            if (pathname.isFile()) {
                return pathname.getName().contains(filter);
            } else {
                return true;
            }
        };
        File ffolder = getFile(folder);
        if (ffolder != null && ffolder.isDirectory()) {
            File[] files = ffolder.listFiles(ff);
            if (files != null && files.length > 0) {
                for (File f : files) {
                    if (f.isFile()) {
                        list.add(f.toURI().toURL());
                    } else if (f.isDirectory() && recursive) {
                        getFileURLsRecursive(f, ff, list);
                    }
                }
            }
        }
        return list;
    }

    private void getFileURLsRecursive(File root, FileFilter ff, List<URL> list) throws MalformedURLException {
        if (root != null && root.isDirectory()) {
            File[] files = root.listFiles(ff);
            if (files != null && files.length > 0) {
                for (File f : files) {
                    if (f.isFile()) {
                        list.add(f.toURI().toURL());
                    } else if (f.isDirectory()) {
                        getFileURLsRecursive(f, ff, list);
                    }
                }
            }
        }

    }

    @Override
    public List<JSONObject> getJSONFiles(String folder, boolean recursive) {
        List<JSONObject> list = new ArrayList<>();
        List<URL> urls;
        try {
            urls = getFileURLs(folder, ".json", recursive);
            LOG.debug("found {} files", urls.size());
        } catch (IOException e1) {
            urls = new ArrayList<>();
            LOG.warn("failed to get urls from resfolder {} : {}", folder, e1.getMessage());
        }
        for (URL u : urls) {
            LOG.debug("try to parse " + u.toString());
            try {
                JSONObject o = new JSONObject(readFile(u));
                list.add(o);
            } catch (JSONException | IOException e) {
                LOG.warn("problem reading/parsing file {} : {}", u, e.getMessage());
            }
        }
        return list;
    }

    @Override
    public JSONObject getJSONFile(String resFile) {
        LOG.debug("loading json file {} from res", resFile);
        URL u = getFileURL(resFile);
        if (u == null) {
            LOG.warn("cannot find resfile: {}", resFile);
            return null;
        }
        JSONObject o = null;
        try {
            // parse to jsonobject
            o = new JSONObject(readFile(u));
        } catch (Exception e) {
            LOG.warn("problem reading/parsing file: {}", e.getMessage());
        }
        return o;
    }

    /**
     * Used for reading plugins from resource files /elasticsearch/plugins/head
     * /etc/elasticsearch-plugins /elasticsearch/plugins
     *
     * @param resFolder resource folder pointing to the related files
     * @param dstFolder destination
     * @param rootDirToRemove part from full path to remove
     * @return true if files could be extracted
     */
    @Override
    public boolean copyFolderInto(String resFolder, String dstFolder, String rootDirToRemove) {

        Enumeration<URL> urls = null;

        LOG.info("Running in file text.");
        urls = getResourceFolderFiles(resFolder);

        boolean success = true;
        URL srcUrl;
        String srcFilename;
        String dstFilename;
        while (urls.hasMoreElements()) {
            srcUrl = urls.nextElement();
            srcFilename = srcUrl.getFile();

            if (srcFilename.endsWith("/")) {
                LOG.debug("Skip directory: {}", srcFilename);
                continue;
            }

            LOG.debug("try to copy res {} to {}", srcFilename, dstFolder);
            if (rootDirToRemove != null) {
                srcFilename =
                        srcFilename.substring(srcFilename.indexOf(rootDirToRemove) + rootDirToRemove.length() + 1);
                LOG.debug("dstfilename trimmed to {}", srcFilename);
            }
            dstFilename = dstFolder + "/" + srcFilename;
            try {
                if (!extractFileTo(srcUrl, new File(dstFilename))) {
                    success = false;
                }
            } catch (Exception e) {
                LOG.warn("problem copying res {} to {}: {}", srcFilename, dstFilename, e.getMessage());
            }
        }

        return success;

    }

    private Enumeration<URL> getResourceFolderFiles(String folder) {
        LOG.debug("Get resource: {}", folder);
        URL url = getUrlForRessource(folder);
        String path = url.getPath();
        File[] files = new File(path).listFiles();
        Collection<URL> urlCollection = new ArrayList<>();

        if (files != null) {
            for (File f : files) {
                try {
                    if (f.isDirectory()) {
                        urlCollection.addAll(Collections.list(getResourceFolderFiles(folder + "/" + f.getName())));
                    } else {
                        urlCollection.add(f.toURI().toURL());
                    }
                } catch (MalformedURLException e) {
                    LOG.error("Can not read ressources", e);
                    break;
                }
            }
        }

        Enumeration<URL> urls = Collections.enumeration(urlCollection);
        return urls;
    }

    private URL getUrlForRessource(String fileOrDirectory) {
        //ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = ResourcesLighty.class.getClassLoader();
        URL url = loader.getResource(fileOrDirectory);
        return url;
    }

    @Override
    public boolean extractFileTo(String resFile, File oFile) {
        if (oFile == null) {
            return false;
        }
        LOG.debug("try to copy {} from res to {}", resFile, oFile.getAbsolutePath());
        URL u = getFileURL(resFile);
        if (u == null) {
            LOG.warn("cannot find resfile: {}", resFile);
            return false;
        }
        return extractFileTo(u, oFile);
    }

    @Override
    public boolean extractFileTo(URL u, File oFile) {

        if (oFile.isDirectory()) {
            oFile.mkdirs();
            return true;
        } else {
            oFile.getParentFile().mkdirs();
        }

        if (!oFile.exists()) {
            try {
                oFile.createNewFile();
            } catch (IOException e) {
                LOG.warn("problem creating file {}: {}", oFile.getAbsoluteFile(), e.getMessage());
            }
        }
        try (InputStream in = u.openStream(); OutputStream outStream = new FileOutputStream(oFile);) {

            int theInt;
            while ((theInt = in.read()) >= 0) {
                outStream.write(theInt);
            }
            in.close();
            outStream.flush();
            outStream.close();
            LOG.debug("file written successfully");
        } catch (IOException e) {
            LOG.error("problem writing file: {}", e.getMessage());
            return false;
        }
        return true;
    }

}

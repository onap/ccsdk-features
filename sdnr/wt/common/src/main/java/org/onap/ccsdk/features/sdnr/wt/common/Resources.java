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
package org.onap.ccsdk.features.sdnr.wt.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resources {

    private static final Logger LOG = LoggerFactory.getLogger(Resources.class);

    private static final String RESSOURCEROOT = "src/main/resources";

    private static URL getFileURL(Class<?> cls,String resFile) {
        Bundle b = FrameworkUtil.getBundle(cls);
        URL u = null;
        LOG.debug("try to get file {}", resFile);
        if (b == null) {
            LOG.info("Load resource as file: {}", resFile);
            u = getUrlForRessource(cls,resFile);
        } else {
            LOG.info("Load resource from bundle: {}", resFile);
            u = b.getEntry(resFile);
        }
        return u;
    }

    private static File getFile(Bundle b,String resFile) {
        File f = null;
        LOG.debug("try to get file {}", resFile);
        if (b == null) {
            LOG.warn("cannot load bundle resources");
            f = new File(RESSOURCEROOT + resFile);
        } else {
            try {
                f = new File(b.getEntry(resFile).toURI());
            } catch (URISyntaxException e) {
                LOG.warn("Con not load file: {}",e.getMessage());
            }
        }
        return f;
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
            sb.append(inputLine+LR);
        }
        in.close();
        s.close();
        return sb.toString();
    }

    private static List<URL> getFileURLs(Bundle b,String folder, final String filter, final boolean recursive)
            throws IOException {

        List<URL> list = new ArrayList<>();
        if (b == null) {
            FileFilter ff = pathname -> {
                if (pathname.isFile()) {
                    return pathname.getName().contains(filter);
                } else {
                    return true;
                }
            };
            File ffolder = getFile(b,folder);
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
        } else {
            getResourceURLsTreeRecurse(b, filter, b.getEntryPaths(folder), recursive, list);
        }
        return list;
    }

    private static void getFileURLsRecursive(File root, FileFilter ff, List<URL> list) throws MalformedURLException {
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

    private static void getResourceURLsTreeRecurse(Bundle b, String filter, Enumeration<String> resource,
            boolean recursive, List<URL> outp) throws IOException {
        while (resource.hasMoreElements()) {
            String name = resource.nextElement();
            Enumeration<String> list = b.getEntryPaths(name);
            if (list != null) {
                if (recursive) {
                    getResourceURLsTreeRecurse(b, filter, list, recursive, outp);
                }
            } else {
                // Read
                if (name.contains(filter)) {
                    LOG.debug("add {} to list", name);
                    outp.add(b.getEntry(name));
                } else {
                    LOG.debug("filtered out {}", name);
                }
            }
        }
    }

//    public static List<JSONObject> getJSONFiles(Bundle b,String folder, boolean recursive) {
//        List<JSONObject> list = new ArrayList<>();
//        List<URL> urls;
//        try {
//            urls = getFileURLs(b,folder, ".json", recursive);
//            LOG.debug("found {} files", urls.size());
//        } catch (IOException e1) {
//            urls = new ArrayList<>();
//            LOG.warn("failed to get urls from resfolder {} : {}", folder, e1.getMessage());
//        }
//        for (URL u : urls) {
//            LOG.debug("try to parse " + u.toString());
//            try {
//                JSONObject o = new JSONObject(readFile(u));
//                list.add(o);
//            } catch (JSONException | IOException e) {
//                LOG.warn("problem reading/parsing file {} : {}", u, e.getMessage());
//            }
//        }
//        return list;
//    }
    public static String getFileContent( Class<?> cls, String resFile) {
         LOG.debug("loading file {} from res", resFile);
         URL u = getFileURL(cls,resFile);
         String s=null;
         if (u == null) {
             LOG.warn("cannot find resfile: {}", resFile);
             return null;
         }
         try {
             s=readFile(u);
         } catch (Exception e) {
             LOG.warn("problem reading file: {}", e.getMessage());
         }
         return s;

    }
//    public static JSONObject getJSONFile(Class<?> cls,String resFile) {
//        LOG.debug("loading json file {} from res", resFile);
//        JSONObject o = null;
//        try {
//            // parse to jsonobject
//            o = new JSONObject(getFileContent(cls,resFile));
//        } catch (Exception e) {
//            LOG.warn("problem reading/parsing file: {}", e.getMessage());
//        }
//        return o;
//    }

    /**
     * Used for reading plugins from resource files /elasticsearch/plugins/head
     * /etc/elasticsearch-plugins /elasticsearch/plugins
     *
     * @param resFolder resource folder pointing to the related files
     * @param dstFolder destination
     * @param rootDirToRemove part from full path to remove
     * @return true if files could be extracted
     */
//    public static boolean copyFolderInto(Bundle b,Class<?> cls,String resFolder, String dstFolder, String rootDirToRemove) {
//
//        Enumeration<URL> urls = null;
//        if (b == null) {
//            LOG.info("Running in file text.");
//            urls = getResourceFolderFiles(cls,resFolder);
//        } else {
//            urls = b.findEntries(resFolder, "*", true);
//        }
//
//        boolean success = true;
//        URL srcUrl;
//        String srcFilename;
//        String dstFilename;
//        while (urls.hasMoreElements()) {
//            srcUrl = urls.nextElement();
//            srcFilename = srcUrl.getFile();
//
//            if (srcFilename.endsWith("/")) {
//                LOG.debug("Skip directory: {}", srcFilename);
//                continue;
//            }
//
//            LOG.debug("try to copy res {} to {}", srcFilename, dstFolder);
//            if (rootDirToRemove != null) {
//                srcFilename =
//                        srcFilename.substring(srcFilename.indexOf(rootDirToRemove) + rootDirToRemove.length() + 1);
//                LOG.debug("dstfilename trimmed to {}", srcFilename);
//            }
//            dstFilename = dstFolder + "/" + srcFilename;
//            try {
//                if (!extractFileTo(srcUrl, new File(dstFilename))) {
//                    success = false;
//                }
//            } catch (Exception e) {
//                LOG.warn("problem copying res {} to {}: {}", srcFilename, dstFilename, e.getMessage());
//            }
//        }
//
//        return success;
//
//    }

//    private static Enumeration<URL> getResourceFolderFiles(Class<?> cls,String folder) {
//        LOG.debug("Get resource: {}", folder);
//        Collection<URL> urlCollection = new ArrayList<>();
//        URL url = getUrlForRessource(cls,folder);
//        if(url==null) {
//            return Collections.enumeration(urlCollection);
//        }
//        String path = url.getPath();
//        File[] files = new File(path).listFiles();
//
//        if (files != null) {
//            for (File f : files) {
//                try {
//                    if (f.isDirectory()) {
//                        urlCollection.addAll(Collections.list(getResourceFolderFiles(cls,folder + "/" + f.getName())));
//                    } else {
//                        urlCollection.add(f.toURI().toURL());
//                    }
//                } catch (MalformedURLException e) {
//                    LOG.error("Can not read ressources", e);
//                    break;
//                }
//            }
//        }
//
//        return Collections.enumeration(urlCollection);
//
//    }

    public static URL getUrlForRessource(Class<?> cls,String fileOrDirectory) {
        //ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = cls.getClassLoader();
        URL url = loader.getResource(fileOrDirectory);
        if(url==null && fileOrDirectory.startsWith("/")) {
            url = loader.getResource(fileOrDirectory.substring(1));
        }
        return url;
    }

//    public static boolean extractFileTo(Class<?> cls,String resFile, File oFile) {
//        if (oFile == null) {
//            return false;
//        }
//        LOG.debug("try to copy {} from res to {}", resFile, oFile.getAbsolutePath());
//        URL u = getFileURL(cls,resFile);
//        if (u == null) {
//            LOG.warn("cannot find resfile: {}", resFile);
//            return false;
//        }
//        return extractFileTo(u, oFile);
//    }
//
//    public static boolean extractFileTo(URL u, File oFile) {
//
//        if (oFile.isDirectory()) {
//            oFile.mkdirs();
//            return true;
//        } else {
//            oFile.getParentFile().mkdirs();
//        }
//
//        if (!oFile.exists()) {
//            try {
//                oFile.createNewFile();
//            } catch (IOException e) {
//                LOG.warn("problem creating file {}: {}", oFile.getAbsoluteFile(), e.getMessage());
//            }
//        }
//        try (InputStream in = u.openStream(); OutputStream outStream = new FileOutputStream(oFile);) {
//
//            int theInt;
//            while ((theInt = in.read()) >= 0) {
//                outStream.write(theInt);
//            }
//            in.close();
//            outStream.flush();
//            outStream.close();
//            LOG.debug("file written successfully");
//        } catch (IOException e) {
//            LOG.error("problem writing file: {}", e.getMessage());
//            return false;
//        }
//        return true;
//    }

}

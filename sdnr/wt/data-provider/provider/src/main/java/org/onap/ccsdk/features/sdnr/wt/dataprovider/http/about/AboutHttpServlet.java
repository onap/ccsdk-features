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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.onap.ccsdk.features.sdnr.wt.common.Resources;
import org.onap.ccsdk.features.sdnr.wt.common.file.PomFile;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@HttpWhiteboardServletPattern("/about")
@HttpWhiteboardServletName("AboutHttpServlet")
@Component(service = Servlet.class)
public class AboutHttpServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AboutHttpServlet.class);
    private static final String UNKNOWN = "unknown";
    private static final String METAINF_MAVEN = "/META-INF/maven/";
    private static final String EXCEPTION_FORMAT_UNABLE_TO_READ_INNER_POMFILE = "unable to read inner pom file: {}";

    private static final String URI_PRE = "/about";
    private static final String RES_BASEPATH = "about/";

    private static final String PLACEHOLDER_ONAP_RELEASENAME = "{release-name}";
    private static final String PLACEHOLDER_ONAP_RELEASEVERSION = "{release-version}";
    private static final String PLACEHOLDER_ODL_RELEASENAME = "{odl-version}";
    private static final String PLACEHOLDER_BUILD_TIMESTAMP = "{build-time}";
    private static final String PLACEHOLDER_PACKAGE_GITHASH = "{package-githash}";
    private static final String PLACEHOLDER_PACAKGE_VERSION = "{package-version}";
    private static final String PLACEHOLDER_CCSDK_VERSION = "{ccsdk-version}";
    private static final String PLACEHOLDER_CLUSTER_SIZE = "{cluster-size}";
    private static final String PLACEHOLDER_MDSAL_VERSION = "{mdsal-version}";
    private static final String PLACEHOLDER_YANGTOOLS_VERSION = "{yangtools-version}";
    private static final String PLACEHOLDER_KARAF_INFO = "{karaf-info}";
    private static final String PLACEHOLDER_DEVICEMANAGER_TABLE = "{devicemanagers}";
    private static final String README_FILE = "README.md";
    private static final String JSON_FILE = "README.json";
    private static final String NO_DEVICEMANAGERS_RUNNING_MESSAGE = null;
    private static final String MIMETYPE_JSON = "application/json";
    private static final String MIMETYPE_MARKDOWN = "text/markdown";

    private final String groupId = this.getGroupIdOrDefault("org.onap.ccsdk.features.sdnr.wt");
    private final String artifactId = "sdnr-wt-data-provider-provider";

    private final Map<Integer, String> BUNDLESTATE_LUT;
    private final Map<String, String> data;
    private final String readmeContent;
    private final String jsonContent;


    public AboutHttpServlet() {

        this.data = new HashMap<>();
        this.collectStaticData();
        this.readmeContent = this.render(ContentType.MARKDOWN, this.getResourceFileContent(README_FILE));
        this.jsonContent = this.render(ContentType.MARKDOWN, this.getResourceFileContent(JSON_FILE));
        this.BUNDLESTATE_LUT = new HashMap<>();
        this.BUNDLESTATE_LUT.put(Bundle.UNINSTALLED, "uninstalled");
        this.BUNDLESTATE_LUT.put(Bundle.INSTALLED, "installed");
        this.BUNDLESTATE_LUT.put(Bundle.RESOLVED, "resolved");
        this.BUNDLESTATE_LUT.put(Bundle.STARTING, "starting");
        this.BUNDLESTATE_LUT.put(Bundle.STOPPING, "stopping");
        this.BUNDLESTATE_LUT.put(Bundle.ACTIVE, "active");
    }

    protected String getGroupIdOrDefault(String def) {
        String symbolicName = this.getManifestValue("Bundle-SymbolicName");
        if (symbolicName != null) {
            int idx = symbolicName.indexOf(this.artifactId);
            if (idx > 0) {
                return symbolicName.substring(0, idx - 1);
            }
        }
        return def;
    }

    /**
     * collect static versioning data
     */
    private void collectStaticData() {
        final String ccsdkVersion = this.getPomParentVersion();
        final String mdsalVersion = SystemInfo.getMdSalVersion(UNKNOWN);
        this.data.put(PLACEHOLDER_ONAP_RELEASENAME, ODLVersionLUT.getONAPReleaseName(ccsdkVersion, UNKNOWN));
        this.data.put(PLACEHOLDER_ODL_RELEASENAME, ODLVersionLUT.getOdlVersion(mdsalVersion, UNKNOWN));
        this.data.put(PLACEHOLDER_BUILD_TIMESTAMP, getDate(this.getManifestValue("Bnd-LastModified"), UNKNOWN));
        this.data.put(PLACEHOLDER_PACAKGE_VERSION, this.getManifestValue("Bundle-Version"));
        this.data.put(PLACEHOLDER_CCSDK_VERSION, ccsdkVersion);
        this.data.put(PLACEHOLDER_ONAP_RELEASEVERSION, SystemInfo.getOnapVersion(UNKNOWN));
        this.data.put(PLACEHOLDER_MDSAL_VERSION, mdsalVersion);
        this.data.put(PLACEHOLDER_YANGTOOLS_VERSION, SystemInfo.getYangToolsVersion(UNKNOWN));
        this.data.put(PLACEHOLDER_PACKAGE_GITHASH, this.getGitHash(UNKNOWN));
    }

    private String getDate(String value, String defaultValue) {
        if(value==null) {
            return defaultValue;
        }
        try {
            long x = Long.parseLong(value);
            return NetconfTimeStampImpl.getConverter().getTimeStampAsNetconfString(new Date(x));
        }
        catch(NumberFormatException e) {
            LOG.debug("date value is not a numeric one");
        }
        return defaultValue;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String uri = req.getRequestURI().substring(URI_PRE.length());
        LOG.debug("request for {}", uri);
        if (uri.length() <= 0 || uri.equals("/")) {
            ContentType ctype = this.detectContentType(req, ContentType.MARKDOWN);
            // collect data
            this.collectData(ctype);
            // render readme
            String content = this.render(ctype);
            byte[] output = content != null ? content.getBytes() : new byte[0];
            // output
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentLength(output.length);
            resp.setContentType(ctype.getMimeType());
            try (ServletOutputStream os = resp.getOutputStream()) {
                os.write(output);
            } catch (IOException e) {
                LOG.warn("problem writing response for {}: {}", uri, e);
            }
        } else {
            this.doGetFile(uri, resp);
        }
    }

    /**
     * load git.commit.id from jar /META-INF/git.properties
     *
     * @param def
     */
    private String getGitHash(String def) {
        String content = Resources.getFileContent(AboutHttpServlet.class, "/META-INF/git.properties");
        if (content == null) {
            return def;
        }
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith("git.commit.id")) {
                def = line.substring("git.commit.id=".length());
                break;
            }
        }
        return def;
    }

    private String getResourceFileContent(String filename) {
        LOG.debug("try ti get content of {}", filename);
        return Resources.getFileContent(AboutHttpServlet.class, RES_BASEPATH + filename);
    }

    /**
     * collect dynamic data for about.md
     */
    private void collectData(ContentType ctype) {
        LOG.info("collecting dynamic data with content-type {}", ctype);
        try {
            this.data.put(PLACEHOLDER_KARAF_INFO, SystemInfo.get());
            this.data.put(PLACEHOLDER_DEVICEMANAGER_TABLE, this.getDevicemanagerBundles(ctype));
        } catch (Exception e) {
            LOG.warn("problem collecting system data: ", e);
        }
    }

    /**
     * get value for key out of /META-INF/MANIFEST.MF
     *
     * @param key
     * @return
     */
    protected String getManifestValue(String key) {
        URL url = Resources.getUrlForRessource(AboutHttpServlet.class, "/META-INF/MANIFEST.MF");
        if (url == null) {
            return null;
        }
        Manifest manifest;
        try {
            manifest = new Manifest(url.openStream());
            Attributes attr = manifest.getMainAttributes();
            return attr.getValue(key);
        } catch (IOException e) {
            LOG.warn("problem reading manifest: ", e);
        }
        return null;

    }

    /**
     * get parent pom version out of /META-INF/maven/groupId/artifactId/pom.xml
     *
     * @return
     */
    private String getPomParentVersion() {
        LOG.info("try to get pom parent version");
        URL url = Resources.getUrlForRessource(AboutHttpServlet.class,
                METAINF_MAVEN + groupId + "/" + artifactId + "/pom.xml");
        if (url == null) {
            return null;
        }
        PomFile pomfile;
        try {
            pomfile = new PomFile(url.openStream());
            return pomfile.getParentVersion();
        } catch (Exception e) {
            LOG.warn(EXCEPTION_FORMAT_UNABLE_TO_READ_INNER_POMFILE, e);
        }
        return null;
    }

    private String getDevicemanagerBundles(ContentType ctype) {
        Bundle thisbundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext context = thisbundle == null ? null : thisbundle.getBundleContext();
        if (context == null) {
            LOG.debug("no bundle context available");
            return ctype == ContentType.MARKDOWN ? "" : "[]";
        }
        Bundle[] bundles = context.getBundles();
        if (bundles == null || bundles.length <= 0) {
            LOG.debug("no bundles found");
            return ctype == ContentType.MARKDOWN ? NO_DEVICEMANAGERS_RUNNING_MESSAGE : "[]";
        }
        LOG.debug("found {} bundles", bundles.length);
        MarkdownTable table = new MarkdownTable();
        table.setHeader(new String[] {"Bundle-Id", "Version", "Symbolic-Name", "Status"});
        String name;
        for (Bundle bundle : bundles) {
            name = bundle.getSymbolicName();
            if (!(name.contains("devicemanager") && name.contains("provider"))) {
                continue;
            }
            if (name.equals("org.onap.ccsdk.features.sdnr.wt.sdnr-wt-devicemanager-provider")) {
                continue;
            }
            table.addRow(new String[] {String.valueOf(bundle.getBundleId()), bundle.getVersion().toString(), name,
                    BUNDLESTATE_LUT.getOrDefault(bundle.getState(), UNKNOWN)});

        }
        return ctype == ContentType.MARKDOWN ? table.toMarkDown() : table.toJson();
    }

    /**
     * get file by uri from resources and write out to response stream
     *
     * @param uri
     * @param resp
     */
    private void doGetFile(String uri, HttpServletResponse resp) {
        String content = this.getResourceFileContent(uri);
        if (content == null) {
            try {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                LOG.debug("unable to send error response : {}", e);
            }
        } else {
            byte[] data = content.getBytes();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(this.getContentType(uri));
            try {
                resp.getOutputStream().write(data);
            } catch (IOException e) {
                LOG.debug("unable to send data: ", e);
            }
        }

    }

    /**
     * create http response contentType by filename
     *
     * @param filename
     * @return
     */
    private String getContentType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
            case "jpg":
            case "jpeg":
            case "svg":
            case "png":
            case "gif":
            case "bmp":
                return "image/" + ext;
            case "json":
                return MIMETYPE_JSON;
            case "html":
            case "htm":
                return "text/html";
            case "txt":
            case "md":
            default:
                return "text/plain";
        }
    }

    /**
     * render this.readmeContent with this.data
     *
     * @param ctype
     *
     * @return
     */
    private String render(ContentType ctype) {
        return this.render(ctype, null);
    }

    /**
     * render content with this.data
     *
     * @param content
     * @return
     */
    private String render(ContentType ctype, String content) {
        if (content == null) {
            content = ctype == ContentType.MARKDOWN ? this.readmeContent : this.jsonContent;
        }
        if (content == null) {
            return null;
        }
        for (Entry<String, String> entry : this.data.entrySet()) {
            if (entry.getValue() != null && content.contains(entry.getKey())) {
                content = content.replace(entry.getKey(), entry.getValue());
            }
        }

        return content;
    }

    public void setClusterSize(String value) {
        this.data.put(PLACEHOLDER_CLUSTER_SIZE, value);
    }

    private ContentType detectContentType(HttpServletRequest req, ContentType def) {
        String accept = req.getHeader(HttpHeaders.ACCEPT);
        if (accept != null) {
            if (accept.equals(MIMETYPE_JSON)) {
                return ContentType.JSON;
            } else if (accept.equals(MIMETYPE_MARKDOWN)) {
                return ContentType.MARKDOWN;
            }
        }
        return def;
    }

    private enum ContentType {
        MARKDOWN(MIMETYPE_MARKDOWN), JSON(MIMETYPE_JSON);

        private String mimeType;

        ContentType(String mimeType) {
            this.mimeType = mimeType;
        }

        String getMimeType() {
            return this.mimeType;
        }
    }
}

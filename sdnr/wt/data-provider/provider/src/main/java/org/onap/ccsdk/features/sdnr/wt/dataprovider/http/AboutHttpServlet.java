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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;	
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.ccsdk.features.sdnr.wt.common.Resources;
import org.onap.ccsdk.features.sdnr.wt.common.file.PomFile;
import org.onap.ccsdk.features.sdnr.wt.common.file.PomPropertiesFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AboutHttpServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(AboutHttpServlet.class);

	private static final String URI_PRE = "/about";
	private static final String RES_BASEPATH = "about/";

	private static final String PLACEHOLDER_ONAP_RELEASENAME = "{release-name}";
	private static final String PLACEHOLDER_ODL_RELEASENAME = "{odl-version}";
	private static final String PLACEHOLDER_BUILD_TIMESTAMP = "{build-time}";
	private static final String PLACEHOLDER_ODLUX_REVISION = "{odlux-revision}";
	private static final String PLACEHOLDER_PACAKGE_VERSION = "{package-version}";
	private static final String README_FILE = "README.md";

	private final String groupId="org.onap.ccsdk.features.sdnr.wt";
	private final String artifactId="sdnr-wt-data-provider-provider";
	
//	private final ConfigurationFileRepresentation configuration;
	//private final ExtRestClient dbClient;
	private final Map<String, String> data;
	private final String readmeContent;
	
	

	public AboutHttpServlet() {
//		this.configuration = new ConfigurationFileRepresentation(DataProviderServiceImpl.CONFIGURATIONFILE);
//		EsConfig esConfig = new EsConfig(configuration);
		//this.dbClient = ExtRestClient.createInstance(esConfig.getHosts());
		this.data = new HashMap<>();
		this.collectStaticData();
		this.readmeContent = this.render(this.getFileContent(README_FILE));
	}

	private void collectStaticData() {
		PomPropertiesFile props = this.getPomProperties();
		this.data.put(PLACEHOLDER_ONAP_RELEASENAME, this.getPomProperty("onap.distname"));
		this.data.put(PLACEHOLDER_ODL_RELEASENAME,  this.getPomProperty("odl.distname") );
		this.data.put(PLACEHOLDER_BUILD_TIMESTAMP, props!=null?props.getBuildDate().toString():"");
		this.data.put(PLACEHOLDER_ODLUX_REVISION, this.getPomProperty("odlux.buildno"));
		this.data.put(PLACEHOLDER_PACAKGE_VERSION, this.getManifestValue("Bundle-Version"));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String uri = req.getRequestURI().substring(URI_PRE.length());
		LOG.debug("request for {}",uri);
		if (uri.length() <= 0 || uri.equals("/")) {
			// collect data
			this.collectData();
			// render readme
			String content = this.render();
			byte[] output = content!=null?content.getBytes():new byte[0];
			// output
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLength(output.length);
			resp.setContentType("text/plain");
			resp.getOutputStream().write(output);

		} else {
			this.doGetFile(uri, resp);
		}
	}

	private String getFileContent(String filename) {
		LOG.debug("try ti get content of {}",filename);
		return Resources.getFileContent(AboutHttpServlet.class,RES_BASEPATH + filename);
	}

	private void collectData() {
		// TODO Auto-generated method stub

	}

	private String getManifestValue(String key) {
		URL url = Resources.getUrlForRessource(AboutHttpServlet.class, "/META-INF/MANIFEST.MF");
		if(url==null) {
			return null;
		}
		Manifest manifest;
		try {
			manifest = new Manifest(url.openStream());
			Attributes attr = manifest.getMainAttributes();
			return attr.getValue(key);
		} catch (IOException e) {
			LOG.warn("problem reading manifest: {}",e);
		}
		return null;

	}
	private PomPropertiesFile getPomProperties() {
		URL url = Resources.getUrlForRessource(AboutHttpServlet.class, "/META-INF/maven/"+groupId+"/"+artifactId+"/pom.properties");
		PomPropertiesFile propfile;
		if(url==null) {
			return null;
		}
		try {
			propfile = new PomPropertiesFile(url.openStream());
			return propfile;
		} catch (Exception e) {
			LOG.warn("unable to read inner pom file: {}",e);
		}
		return null;
	}
	private String getPomProperty(String key) {
		LOG.info("try to get pom property for {}",key);
		URL url = Resources.getUrlForRessource(AboutHttpServlet.class,"/META-INF/maven/"+groupId+"/"+artifactId+"/pom.xml");
		if(url==null) {
			return null;
		}
		PomFile pomfile;
		try {
			pomfile = new PomFile(url.openStream());
			return pomfile.getProperty(key);
		} catch (Exception e) {
			LOG.warn("unable to read inner pom file: {}",e);
		}
		return null;
	}
	private void doGetFile(String uri, HttpServletResponse resp) {
		String content = this.getFileContent(uri);
		if (content == null) {
			try {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				LOG.debug("unable to send error response : {}",e);
			}
		} else {
			byte[] data = content.getBytes();
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType(this.getContentType(uri));
			try {
				resp.getOutputStream().write(data);
			} catch (IOException e) {
				LOG.debug("unable to send data : {}",e);
			}
		}

	}

	private String getContentType(String filename) {
		String ext = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
		switch (ext) {
		case "jpg":
		case "jpeg":
		case "svg":
		case "png":
		case "gif":
		case "bmp":
			return "image/" + ext;
		case "json":
			return "application/json";
		case "html":
		case "htm":
			return "text/html";
		case "txt":
		case "md":
		default:
			return "text/plain";
		}
	}

	private String render() {
		return this.render(null);
	}

	private String render(String content) {
		if (content == null) {
			content = this.readmeContent;
		}
		if(content==null) {
			return null;
		}
		for (Entry<String, String> entry : this.data.entrySet()) {
			if (entry.getValue() != null && content.contains(entry.getKey())) {
				content = content.replace(entry.getKey(), entry.getValue());
			}
		}

		return content;
	}
}
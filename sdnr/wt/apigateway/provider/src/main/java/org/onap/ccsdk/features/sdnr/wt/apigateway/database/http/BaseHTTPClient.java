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
package org.onap.ccsdk.features.sdnr.wt.apigateway.database.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseHTTPClient {

	private static Logger LOG = LoggerFactory.getLogger(BaseHTTPClient.class);
	private static final int BUFSIZE = 1024;
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final String SSLCONTEXT = "TLSv1.2";
	private static final int DEFAULT_HTTP_TIMEOUT_MS = 30000; // in ms

	private final boolean trustAll;
	private String baseUrl;

	private int timeout = DEFAULT_HTTP_TIMEOUT_MS;
	private SSLContext sc = null;

	public BaseHTTPClient(String base) {
		this(base, false);
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		try {
			sc = setupSsl(trustAll);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			LOG.warn("problem ssl setup: " + e.getMessage());
		}
	}

	public BaseHTTPClient(String base, boolean trustAllCerts) {
		this.baseUrl = base;
		this.trustAll = trustAllCerts;
		try {
			sc = setupSsl(trustAll);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			LOG.warn("problem ssl setup: " + e.getMessage());
		}
	}

	protected @Nonnull BaseHTTPResponse sendRequest(String uri, String method, String body, Map<String, String> headers)
			throws IOException {
		return this.sendRequest(uri, method, body != null ? body.getBytes(CHARSET) : null, headers);
	}

	protected @Nonnull BaseHTTPResponse sendRequest(String uri, String method, byte[] body, Map<String, String> headers)
			throws IOException {
		if (uri == null) {
			uri = "";
		}
		String surl = this.baseUrl;
		if (!surl.endsWith("/") && uri.length() > 0) {
			surl += "/";
		}
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		surl += uri;
		LOG.debug("try to send request with url=" + this.baseUrl + uri + " as method=" + method);
		LOG.trace("body:" + (body == null ? "null" : new String(body, CHARSET)));
		URL url = new URL(surl);
		URLConnection http = url.openConnection();
		http.setConnectTimeout(this.timeout);
		if (surl.toString().startsWith("https")) {
			if (sc != null) {
				((HttpsURLConnection) http).setSSLSocketFactory(sc.getSocketFactory());
				if (trustAll) {
					LOG.debug("trusting all certs");
					HostnameVerifier allHostsValid = (hostname, session) -> true;
					((HttpsURLConnection) http).setHostnameVerifier(allHostsValid);
				}
			} else // Should never happen
			{
				LOG.warn("No SSL context available");
				return new BaseHTTPResponse(-1, "");
			}
		}
		((HttpURLConnection) http).setRequestMethod(method);
		http.setDoOutput(true);
		if (headers != null && headers.size() > 0) {
			for (String key : headers.keySet()) {
				http.setRequestProperty(key, headers.get(key));
				LOG.trace("set http header " + key + ": " + headers.get(key));
			}
		}
		byte[] buffer = new byte[BUFSIZE];
		int len = 0, lensum = 0;
		// send request
		// Send the message to destination
		if (!method.equals("GET") && body != null && body.length > 0) {
			try (OutputStream output = http.getOutputStream()) {
				output.write(body);
			}
		}
		// Receive answer
		int responseCode = ((HttpURLConnection) http).getResponseCode();
		String sresponse = "";
		InputStream response = null;
		try {
			if (responseCode >= 200 && responseCode < 300) {
				response = http.getInputStream();
			} else {
				response = ((HttpURLConnection) http).getErrorStream();
				if (response == null) {
					response = http.getInputStream();
				}
			}
			if (response != null) {
				while (true) {
					len = response.read(buffer, 0, BUFSIZE);
					if (len <= 0) {
						break;
					}
					lensum += len;
					sresponse += new String(buffer, 0, len, CHARSET);
				}
			} else {
				LOG.debug("response is null");
			}
		} catch (Exception e) {
			LOG.debug("No response. ", e);
		} finally {
			if (response != null) {
				response.close();
			}
		}
		LOG.debug("ResponseCode: " + responseCode);
		LOG.trace("Response (len:{}): {}", String.valueOf(lensum), sresponse);
		return new BaseHTTPResponse(responseCode, sresponse);
	}

	public static SSLContext setupSsl(boolean trustall) throws KeyManagementException, NoSuchAlgorithmException {

		SSLContext sc = SSLContext.getInstance(SSLCONTEXT);
		TrustManager[] trustCerts = null;
		if (trustall) {
			trustCerts = new TrustManager[] { new javax.net.ssl.X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

		}
		KeyManager[] kms = null;
		// Init the SSLContext with a TrustManager[] and SecureRandom()
		sc.init(kms, trustCerts, new java.security.SecureRandom());
		return sc;
	}

	public static String getAuthorizationHeaderValue(String username, String password) {
		return "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
	}

}

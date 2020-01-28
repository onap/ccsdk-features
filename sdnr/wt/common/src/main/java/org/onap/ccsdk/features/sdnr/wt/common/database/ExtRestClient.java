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
package org.onap.ccsdk.features.sdnr.wt.common.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.json.JSONException;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo.Protocol;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ClusterHealthRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.IndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.IndicesAliasesRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.NodeStatsRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.RefreshIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AcknowledgedResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ClusterHealthResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.CreateIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.GetResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.IndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.NodeStatsResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.RefreshIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtRestClient {

	private static final Logger LOG = LoggerFactory.getLogger(ExtRestClient.class);

	private class BasicAuthHttpClientConfigCallback implements HttpClientConfigCallback {

		private final String basicAuthUsername;
		private final String basicAuthPassword;

		BasicAuthHttpClientConfigCallback(String username, String password) {
			this.basicAuthUsername = username;
			this.basicAuthPassword = password;
		}

		@Override
		public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
			if (basicAuthPassword == null || basicAuthUsername == null) {
				return httpClientBuilder;
			}
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(basicAuthUsername, basicAuthPassword));

			return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		}

	}
	private class SSLCercAuthHttpClientConfigCallback implements HttpClientConfigCallback {

		private final String certFilename;

		SSLCercAuthHttpClientConfigCallback(String certfile) {
			this.certFilename = certfile;
		}

		@Override
		public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
			if (this.certFilename == null) {
				return httpClientBuilder;
			}

			char[] keystorePass = "MY PASSWORD".toCharArray();

			FileInputStream fis = null;

			// Loading KEYSTORE in JKS format
			KeyStore keyStorePci = null;
			try {
				keyStorePci = KeyStore.getInstance(KeyStore.getDefaultType());
			} catch (KeyStoreException e1) {
				LOG.warn("unable to load keystore: {}",e1);
			}
			if (keyStorePci != null) {
				try {
					fis = new FileInputStream(this.certFilename);
					keyStorePci.load(fis, keystorePass);
				} catch (Exception e) {
					LOG.error("Error loading keystore: " + this.certFilename);
				} finally {
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {

						}
					}
				}
			}
			SSLContext sslcontext=null;
			try {
				sslcontext = SSLContexts.custom().loadKeyMaterial(keyStorePci, keystorePass).build();
			} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException
					| KeyStoreException e) {
				LOG.warn("unable to load sslcontext: {}",e);
			}
			return httpClientBuilder.setSSLContext(sslcontext);
		}
	}

	private RestClient client;

	protected ExtRestClient(HostInfo[] hosts) {
		this(hosts, null, null);
	}
	protected ExtRestClient(HostInfo[] hosts,String username,String password) {
		this.client = RestClient.builder(get(hosts)).setHttpClientConfigCallback(new BasicAuthHttpClientConfigCallback(username, password) ).build();
	}

	public ClusterHealthResponse health(ClusterHealthRequest request)
			throws UnsupportedOperationException, IOException, JSONException {
		return new ClusterHealthResponse(this.client.performRequest(request.getInner()));
	}

	public void close() throws IOException {
		this.client.close();
		
	}
	//
	public boolean indicesExists(GetIndexRequest request) throws IOException {
		Response response = this.client.performRequest(request.getInner());
		return response.getStatusLine().getStatusCode()==200;
	}

	public AcknowledgedResponse updateAliases(IndicesAliasesRequest request) throws IOException{
		return new AcknowledgedResponse(this.client.performRequest(request.getInner()));
	}

	public CreateIndexResponse createIndex(CreateIndexRequest request) throws IOException {
		CreateIndexResponse response = new CreateIndexResponse(this.client.performRequest(request.getInner()));
		return response;
	}
	public DeleteIndexResponse deleteIndex(DeleteIndexRequest request) throws IOException {
		return new DeleteIndexResponse(this.client.performRequest(request.getInner()));
	}
	public IndexResponse index(IndexRequest request) throws IOException{
		return new IndexResponse(this.client.performRequest(request.getInner()));
	}

	public DeleteResponse delete(DeleteRequest request) throws IOException{
		Response response=null;
		try {
			 response = this.client.performRequest(request.getInner());
		}
		catch(ResponseException e) {
			new DeleteResponse(e.getResponse());
		}
		return new DeleteResponse(response);
	}
	public DeleteByQueryResponse deleteByQuery(DeleteByQueryRequest request) throws IOException {
		Response response=null;
		try {
			 response = this.client.performRequest(request.getInner());
		}
		catch(ResponseException e) {
			new DeleteResponse(e.getResponse());
		}
		return new DeleteByQueryResponse(response);
	}
	public SearchResponse search(SearchRequest request) throws IOException{
		return this.search(request,false);
	}

	/**
	 * 
	 * @param request
	 * @param ignoreParseException especially for usercreated filters which may cause ES server response exceptions
	 * @return
	 * @throws IOException
	 */
	public SearchResponse search(SearchRequest request, boolean ignoreParseException) throws IOException {
		if (ignoreParseException) {
			try {
				return new SearchResponse(this.client.performRequest(request.getInner()));
			} catch (ResponseException e) {
				LOG.debug("ignoring Exception for request {}: {}",request,e.getMessage());
				return new SearchResponse(e.getResponse());
			}
		} else {
			return new SearchResponse(this.client.performRequest(request.getInner()));
		}
	}

	public GetResponse get(GetRequest request) throws IOException{
		try {
			return new GetResponse(this.client.performRequest(request.getInner()));
		}
		catch (ResponseException e) {
			return new GetResponse(e.getResponse());
		}
	}
	

	public UpdateByQueryResponse update(UpdateByQueryRequest request) throws IOException {
		return new UpdateByQueryResponse(this.client.performRequest(request.getInner()));
		
	}
	public UpdateResponse update(UpdateRequest request) throws IOException {
		return new UpdateResponse(this.client.performRequest(request.getInner()));
		
	}
	public RefreshIndexResponse refreshIndex(RefreshIndexRequest request) throws IOException{
		return new RefreshIndexResponse(this.client.performRequest(request.getInner()));
	}
	
	public NodeStatsResponse stats(NodeStatsRequest request) throws IOException{
		return new NodeStatsResponse(this.client.performRequest(request.getInner()));
	}
	
	public boolean waitForYellowStatus(long timeoutms) {
	
		ClusterHealthRequest request = new ClusterHealthRequest();
		request.timeout(timeoutms/1000);
		ClusterHealthResponse response = null;
		String status="";
		try {
			response = this.health(request);

		} catch (UnsupportedOperationException | IOException | JSONException e) {
			LOG.error(e.getMessage());
		}
		if(response!=null) {
			status=response.getStatus();
			LOG.debug("Elasticsearch service started with status {}", response.getStatus());

		}
		else {
			LOG.warn("Elasticsearch service not started yet with status {}. current status is {}",status,"none");
			return false;
		}
		return response.isStatusMinimal(ClusterHealthResponse.HEALTHSTATUS_YELLOW);

	}
	
	private static HttpHost[] get(HostInfo[] hosts) {
		HttpHost[] httphosts = new HttpHost[hosts.length];
		for(int i=0;i<hosts.length;i++) {
			httphosts[i]=new HttpHost(hosts[i].hostname, hosts[i].port, hosts[i].protocol.toString());
		}
		return httphosts;
	}
	public static ExtRestClient createInstance(HostInfo[] hosts) {
		return new ExtRestClient(hosts);
	}
	public static ExtRestClient createInstance(HostInfo[] hosts,String username,String password) {
		return new ExtRestClient(hosts,username,password);
	}
	public static ExtRestClient createInstance(String hostname, int port, Protocol protocol){
		return createInstance(new HostInfo[] {new HostInfo(hostname,port,protocol)});

	}
	

}

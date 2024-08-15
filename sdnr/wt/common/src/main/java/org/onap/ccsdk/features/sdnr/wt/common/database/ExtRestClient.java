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
package org.onap.ccsdk.features.sdnr.wt.common.database;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.json.JSONException;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo.Protocol;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ClusterHealthRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ClusterSettingsRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateAliasRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.Delete7Request;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteAliasRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.Get7Request;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetInfoRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.Index7Request;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.IndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ListAliasesRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ListIndicesRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.NodeStatsRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.RefreshIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.Search7Request;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.Update7Request;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateByQuery7Request;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ClusterHealthResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ClusterSettingsResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.CreateAliasResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.CreateIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteAliasResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.GetInfoResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.GetResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.IndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ListAliasesResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ListIndicesResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.NodeStatsResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.RefreshIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateResponse;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ExtRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(ExtRestClient.class);

    private class BasicAuthHttpClientConfigCallback implements HttpClientConfigCallback {

        private final String basicAuthUsername;
        private final String basicAuthPassword;
        private final boolean trustAll;

        BasicAuthHttpClientConfigCallback(String username, String password, boolean trustAll) {
            this.basicAuthUsername = username;
            this.basicAuthPassword = password;
            this.trustAll = trustAll;
        }

        @Override
        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
            HttpAsyncClientBuilder httpAsyncClientBuilder = null;
            try {
                httpAsyncClientBuilder = httpClientBuilder.setSSLContext(BaseHTTPClient.setupSsl(this.trustAll));
                if (this.trustAll) {
                    httpAsyncClientBuilder.setSSLHostnameVerifier(new HostnameVerifier() {

                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }
            } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException
                    | CertificateException | KeyStoreException | InvalidKeySpecException | IOException e) {
                LOG.warn("unable to init ssl context for db client: {}", e.getMessage());
            }
            if (basicAuthPassword == null || basicAuthUsername == null) {
                return httpAsyncClientBuilder;
            }
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(basicAuthUsername, basicAuthPassword));

            return httpAsyncClientBuilder == null ? httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                    : httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

    }

    private final RestClient client;
    private final boolean isES7;

    protected ExtRestClient(HostInfo[] hosts) throws Exception {
        this(hosts, null, null, false);
    }

    protected ExtRestClient(HostInfo[] hosts, String username, String password, boolean trustAll) throws Exception {
        this.client = RestClient.builder(get(hosts))
                .setHttpClientConfigCallback(new BasicAuthHttpClientConfigCallback(username, password, trustAll))
                .build();
        DatabaseVersion tmp = autoDetectVersion();
        LOG.info("working with sdnrdb version {}", tmp.toString());
        this.isES7 = tmp.isNewerOrEqualThan(new DatabaseVersion(7, 0, 0));
    }

    /**
     * @return
     * @throws IOException
     * @throws Exception
     */
    private DatabaseVersion autoDetectVersion() throws IOException, Exception {
        GetInfoResponse infoResponse = this.getInfo();
        return infoResponse.getVersion();

    }

    public boolean isVersion7() {
        return this.isES7;
    }

    public ClusterHealthResponse health(ClusterHealthRequest request)
            throws UnsupportedOperationException, IOException, JSONException {
        return new ClusterHealthResponse(this.client.performRequest(request.getInner()));
    }

    public void close() throws IOException {
        this.client.close();

    }

    public boolean indicesExists(GetIndexRequest request) throws IOException {
        Response response = this.client.performRequest(request.getInner());
        return response.getStatusLine().getStatusCode() == 200;
    }

    public ClusterSettingsResponse setupClusterSettings(ClusterSettingsRequest request) throws IOException {
        return new ClusterSettingsResponse(this.client.performRequest(request.getInner()));
    }

    public CreateAliasResponse updateAliases(CreateAliasRequest request) throws IOException {
        return new CreateAliasResponse(this.client.performRequest(request.getInner()));
    }

    public CreateIndexResponse createIndex(CreateIndexRequest request) throws IOException {
        return new CreateIndexResponse(this.client.performRequest(request.getInner()));
    }

    public CreateAliasResponse createAlias(CreateAliasRequest request) throws IOException {
        return new CreateAliasResponse(this.client.performRequest(request.getInner()));
    }

    public DeleteAliasResponse deleteAlias(DeleteAliasRequest request) throws IOException {
        return new DeleteAliasResponse(this.client.performRequest(request.getInner()));
    }

    public DeleteIndexResponse deleteIndex(DeleteIndexRequest request) throws IOException {
        return new DeleteIndexResponse(this.client.performRequest(request.getInner()));
    }

    public IndexResponse index(IndexRequest request) throws IOException {
        if (this.isES7 && !(request instanceof Index7Request)) {
            request = new Index7Request(request);
        }
        return new IndexResponse(this.client.performRequest(request.getInner()));
    }

    public DeleteResponse delete(DeleteRequest request) throws IOException {
        Response response = null;
        if (this.isES7 && !(request instanceof Delete7Request)) {
            request = new Delete7Request(request);
        }
        try {
            response = this.client.performRequest(request.getInner());
        } catch (ResponseException e) {
            new DeleteResponse(e.getResponse());
        }
        return new DeleteResponse(response);
    }

    public DeleteByQueryResponse deleteByQuery(DeleteByQueryRequest request) throws IOException {
        Response response = null;
        try {
            response = this.client.performRequest(request.getInner());
        } catch (ResponseException e) {
            new DeleteResponse(e.getResponse());
        }
        return new DeleteByQueryResponse(response);
    }

    public SearchResponse search(SearchRequest request) throws IOException {
        return this.search(request, false);
    }

    /**
     * Search for database entries
     *
     * @param request inputRequest
     * @param ignoreParseException especially for usercreated filters which may cause ES server response exceptions
     * @return Response with related entries
     * @throws IOException of client
     */
    public SearchResponse search(SearchRequest request, boolean ignoreParseException) throws IOException {
        if (this.isES7 && !(request instanceof Search7Request)) {
            request = new Search7Request(request);
        }
        if (ignoreParseException) {
            try {
                return new SearchResponse(this.client.performRequest(request.getInner()));
            } catch (ResponseException e) {
                LOG.debug("ignoring Exception for request {}: {}", request, e.getMessage());
                return new SearchResponse(e.getResponse());
            }
        } else {
            return new SearchResponse(this.client.performRequest(request.getInner()));
        }
    }

    public GetResponse get(GetRequest request) throws IOException {
        if (this.isES7 && !(request instanceof Get7Request)) {
            request = new Get7Request(request);
        }
        try {
            return new GetResponse(this.client.performRequest(request.getInner()));
        } catch (ResponseException e) {
            return new GetResponse(e.getResponse());
        }
    }

    public UpdateByQueryResponse update(UpdateByQueryRequest request) throws IOException {
        if (this.isES7 && !(request instanceof UpdateByQuery7Request)) {
            request = new UpdateByQuery7Request(request);
        }
        return new UpdateByQueryResponse(this.client.performRequest(request.getInner()));

    }

    public UpdateResponse update(UpdateRequest request) throws IOException {
        if (this.isES7 && !(request instanceof Update7Request)) {
            request = new Update7Request(request);
        }
        return new UpdateResponse(this.client.performRequest(request.getInner()));

    }

    public RefreshIndexResponse refreshIndex(RefreshIndexRequest request) throws IOException {
        return new RefreshIndexResponse(this.client.performRequest(request.getInner()));
    }

    public NodeStatsResponse stats(NodeStatsRequest request) throws IOException {
        return new NodeStatsResponse(this.client.performRequest(request.getInner()));
    }

    public ListIndicesResponse getIndices() throws ParseException, IOException {
        return new ListIndicesResponse(this.client.performRequest(new ListIndicesRequest().getInner()));
    }

    public ListAliasesResponse getAliases() throws ParseException, IOException {
        return new ListAliasesResponse(this.client.performRequest(new ListAliasesRequest().getInner()));
    }

    public GetInfoResponse getInfo() throws IOException, Exception {
        return new GetInfoResponse(this.client.performRequest(new GetInfoRequest().getInner()));
    }

    public boolean waitForYellowStatus(long timeoutms) {

        ClusterHealthRequest request = new ClusterHealthRequest();
        request.timeout(timeoutms / 1000);
        ClusterHealthResponse response = null;
        String status = "";
        try {
            response = this.health(request);

        } catch (UnsupportedOperationException | IOException | JSONException e) {
            LOG.error("Exception", e);
        }
        if (response != null) {
            status = response.getStatus();
            LOG.debug("Elasticsearch service started with status {}", response.getStatus());

        } else {
            LOG.warn("Elasticsearch service not started yet with status {}. current status is {}", status, "none");
            return false;
        }
        return response.isStatusMinimal(ClusterHealthResponse.HEALTHSTATUS_YELLOW);

    }

    private static HttpHost[] get(HostInfo[] hosts) {
        HttpHost[] httphosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            httphosts[i] = new HttpHost(hosts[i].hostname, hosts[i].port, hosts[i].protocol.toString());
        }
        return httphosts;
    }

    public static ExtRestClient createInstance(HostInfo[] hosts) throws Exception {
        return new ExtRestClient(hosts);
    }

    public static ExtRestClient createInstance(HostInfo[] hosts, String username, String password, boolean trustAll)
            throws Exception {
        return new ExtRestClient(hosts, username, password, trustAll);
    }

    public static ExtRestClient createInstance(String hostname, int port, Protocol protocol) throws Exception {
        return createInstance(new HostInfo[] {new HostInfo(hostname, port, protocol)});

    }

}

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
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.IndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.GetResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.IndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Herbert, Micha
 *
 *         Hint: Elasticsearch index/alias and doctype will be the same value server side restricted since ES 5.0
 *
 */

@Deprecated
public class HtDatabaseClient extends ExtRestClient implements DatabaseClient, AutoCloseable {

    private static final boolean REFRESH_AFTER_REWRITE_DEFAULT = true;
    public static final boolean TRUSTALL_DEFAULT = false;
    private static final long TIMOUT_MS_DEFAULT = 30000;
    private static final long READ_MAX_SIZE = 9999;
    private final static long SLEEPTIMEMS = 5000;
    private static final boolean FULLSIZEREQUEST_DEFAULT = false;

    private final Logger LOG = LoggerFactory.getLogger(HtDatabaseClient.class);

    private boolean doRefreshAfterWrite;

    private HtDatabaseClient(HostInfo[] hosts, boolean refreshAfterWrite, String username, String password,
            boolean trustAll) throws Exception {
        super(hosts, username, password, trustAll);
        this.doRefreshAfterWrite = refreshAfterWrite;
    }

    /*----------------------------------
     * static factory function
     */

    static public HtDatabaseClient getClient(HostInfo[] hosts) throws HtDatabaseClientException {
        return getClient(hosts, REFRESH_AFTER_REWRITE_DEFAULT, null, null, TRUSTALL_DEFAULT, TIMOUT_MS_DEFAULT);
    }

    static public HtDatabaseClient getClient(HostInfo[] hosts, String username, String password)
            throws HtDatabaseClientException {
        return getClient(hosts, REFRESH_AFTER_REWRITE_DEFAULT, username, password, TRUSTALL_DEFAULT, TIMOUT_MS_DEFAULT);
    }

    static public HtDatabaseClient getClient(HostInfo[] hosts, String username, String password, boolean trustAll)
            throws HtDatabaseClientException {
        return getClient(hosts, REFRESH_AFTER_REWRITE_DEFAULT, username, password, trustAll, TIMOUT_MS_DEFAULT);
    }

    static public HtDatabaseClient getClient(HostInfo[] hosts, String username, String password, boolean trustAll,
            boolean doFullsizeRequests, long timeoutms) throws HtDatabaseClientException {
        return getClient(hosts, REFRESH_AFTER_REWRITE_DEFAULT, username, password, trustAll, TIMOUT_MS_DEFAULT);
    }

    static public HtDatabaseClient getClient(HostInfo[] hosts, boolean refreshAfterWrite, String username,
            String password, boolean trustAll, long timeoutms) throws HtDatabaseClientException {
        long waiting = 0;
        HtDatabaseClient client = null;
        Exception exceptionWas = null;

        do {
            try {
                client = new HtDatabaseClient(hosts, refreshAfterWrite, username, password, trustAll);
                exceptionWas = null;
            } catch (Exception e) {
                try {
                    Thread.sleep(SLEEPTIMEMS);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                waiting += SLEEPTIMEMS;
                exceptionWas = e;
            }
        } while (client == null && waiting < timeoutms);
        if (client == null) {
            throw new HtDatabaseClientException("Can not reach database server. Client not started.", exceptionWas);
        }
        return client;
    }

    /*----------------------------------
     * Functions
     */

    /**
     * Close function
     */
    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException e) {
            LOG.warn("Problem closing db client: {}", e);
        }
    }

    @Override
    public boolean isExistsIndex(String esIndexAlias) {

        LOG.debug("Check status of ES index: {}", esIndexAlias);

        GetIndexRequest request = new GetIndexRequest(esIndexAlias);

        boolean indexStatus = false;
        try {
            indexStatus = this.indicesExists(request);
        } catch (IOException e) {
            LOG.warn("Problem checking index for {}: {}", esIndexAlias, e);
        }

        return indexStatus;

    }

    @Override
    public @Nullable String doWriteJsonString(String dataTypeName, @Nonnull IsEsObject esId, String json) {
        return doWriteRaw(dataTypeName, esId.getEsId(), json);
    }

    @Override
    public @Nullable String doWriteRaw(String dataTypeName, @Nullable String esId, String json) {
        return this.doWriteRaw(dataTypeName, dataTypeName, esId, json);
    }

    @Override
    public String doWriteRaw(String dataTypeName, String esId, String json, boolean syncAfterWrite) {
        return this.doWriteRaw(dataTypeName, dataTypeName, esId, json, syncAfterWrite);
    }

    @Override
    public @Nullable String doWriteRaw(String indexName, String dataTypeName, @Nullable String esId, String json) {
        return this.doWriteRaw(indexName, dataTypeName, esId, json, this.doRefreshAfterWrite);
    }

    @Override
    public String doWriteRaw(String indexName, String dataTypeName, String esId, String json, boolean syncAfterWrite) {

        IndexResponse response = null;
        IndexRequest indexRequest = new IndexRequest(indexName, dataTypeName, esId, this.doRefreshAfterWrite);
        indexRequest.source(json);
        try {
            response = this.index(indexRequest);
        } catch (IOException e) {
            LOG.warn("ES Exception {} Json: {}", e.getMessage(), json);
        }

        if (response == null) {
            LOG.warn("Response null during write: {} {}", esId, json);
            return null;
        }
        return response.getId();
    }

    @Override
    public boolean doRemove(String dataTypeName, IsEsObject esId) {
        return doRemove(dataTypeName, esId.getEsId());
    }


    @Override
    public boolean doRemove(String dataTypeName, String esId) {
        DeleteRequest deleteRequest = new DeleteRequest(dataTypeName, dataTypeName, esId, this.doRefreshAfterWrite);
        DeleteResponse response = null;
        try {
            response = this.delete(deleteRequest);
        } catch (IOException e) {
            LOG.warn("Problem deleting from db: {}", e.getMessage());
        }
        return response != null ? response.isDeleted() : false;
    }

    @Override
    public @Nullable String doReadJsonData(String dataTypeName, @Nonnull IsEsObject esId) {

        if (esId.getEsId() == null) {
            throw new IllegalArgumentException("Read access to object without database Id");
        }

        return doReadJsonData(dataTypeName, esId.getEsId());
    }

    @Override
    public @Nullable String doReadJsonData(String dataTypeName, @Nonnull String esId) {

        LOG.debug("NetworkIndex read: {}", dataTypeName);
        GetRequest getRequest = new GetRequest(dataTypeName, dataTypeName, esId);
        GetResponse response = null;
        try {
            response = this.get(getRequest);
        } catch (IOException e) {
            LOG.warn("problem reading data {} with id {}: {}", dataTypeName, esId, e);
        }
        return response != null && response.isExists() ? response.getSourceAsBytesRef() : null;
    }

    @Override
    public @Nonnull SearchResult<SearchHit> doReadByQueryJsonData(String dataTypeName, QueryBuilder queryBuilder) {

        return this.doReadByQueryJsonData(dataTypeName, queryBuilder, false);
    }

    @Override
    public @Nonnull SearchResult<SearchHit> doReadByQueryJsonData(String dataTypeName, QueryBuilder queryBuilder,
            boolean ignoreException) {
        return this.doReadByQueryJsonData(dataTypeName, dataTypeName, queryBuilder, ignoreException);
    }

    @Override
    public @Nonnull SearchResult<SearchHit> doReadByQueryJsonData(String alias, String dataTypeName,
            QueryBuilder queryBuilder, boolean ignoreException) {

        long total = 0;
        LOG.debug("NetworkIndex query and read: {}", dataTypeName);
        SearchRequest searchRequest = new SearchRequest(alias, dataTypeName);
        searchRequest.setQuery(queryBuilder);
        SearchResponse response = null;
        try {
            response = this.search(searchRequest, ignoreException);
            total = response.getTotal();

        } catch (IOException e) {
            LOG.warn(
                    "Possible Database connection failure. If this error persists, please check Database connectivity");
            LOG.warn("error do search {}: {}", queryBuilder, e);
        }
        return new SearchResult<SearchHit>(response != null ? response.getHits() : new SearchHit[] {}, total);
    }

    @Override
    public @Nonnull SearchResult<SearchHit> doReadAllJsonData(String dataTypeName) {
        return this.doReadAllJsonData(dataTypeName, false);
    }

    @Override
    public @Nonnull SearchResult<SearchHit> doReadAllJsonData(String dataTypeName, boolean ignoreException) {
        return doReadByQueryJsonData(dataTypeName, QueryBuilders.matchAllQuery().size(READ_MAX_SIZE).from(0),
                ignoreException);
    }

    public @Nonnull SearchResult<SearchHit> doReadAllJsonData(String alias, String dataType, boolean ignoreException) {
        return doReadByQueryJsonData(alias, dataType, QueryBuilders.matchAllQuery(), ignoreException);
    }

    @Override
    public String doUpdateOrCreate(String dataTypeName, String esId, String json) {
        return this.doUpdateOrCreate(dataTypeName, esId, json, null);
    }



    @Override
    public String doUpdateOrCreate(String dataTypeName, String esId, String json, List<String> onlyForInsert) {
        if (esId == null) {
            LOG.warn("try to update or insert {} with id null is not allowed.", dataTypeName);
            return null;
        }
        boolean success = false;
        UpdateRequest request = new UpdateRequest(dataTypeName, dataTypeName, esId, this.doRefreshAfterWrite);
        request.source(new JSONObject(json), onlyForInsert);
        try {
            UpdateResponse response = this.update(request);
            success = response.succeeded();
        } catch (IOException e) {
            LOG.warn("Problem updating {} with id {} and data {}: '{}'", dataTypeName, esId, json, e.getMessage());
        }
        return success ? esId : null;
    }

    @Override
    public boolean doUpdate(String dataTypeName, String json, QueryBuilder query) {
        boolean success = false;
        UpdateByQueryRequest request = new UpdateByQueryRequest(dataTypeName, dataTypeName, this.doRefreshAfterWrite);
        request.source(new JSONObject(json), query);
        try {
            UpdateByQueryResponse response = this.update(request);
            success = !response.hasFailures();
        } catch (IOException e) {
            LOG.warn("Problem updating items in {} with query {} and data {}: {}", dataTypeName, query, json, e);
        }
        return success;
    }



    @Override
    public int doRemove(String dataTypeName, QueryBuilder query) {
        int del = 0;
        DeleteByQueryRequest request = new DeleteByQueryRequest(dataTypeName, this.doRefreshAfterWrite);
        request.source(query);
        try {
            DeleteByQueryResponse response = this.deleteByQuery(request);
            del = response.getDeleted();
        } catch (IOException e) {
            LOG.warn("Problem delete in {} with query {}:{} ", dataTypeName, query.toJSON(), e);
        }
        return del;
    }



}

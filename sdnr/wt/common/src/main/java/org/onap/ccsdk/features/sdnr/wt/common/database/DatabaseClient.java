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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;

/**
 * Access elasticsearch database
 */

public interface DatabaseClient {

    /**
     * Read JSON Object from database
     * 
     * @param dataTypeName to read
     * @param esId to provide id to read
     * @return String with json structure
     */
    public @Nullable String doReadJsonData(String dataTypeName, @Nonnull IsEsObject esId);

    /**
     * Read JSON Object from database
     * 
     * @param dataTypeName to read
     * @param esId of object to read
     * @return String with json structure
     */
    public @Nullable String doReadJsonData(String dataTypeName, @Nonnull String esId);

    /**
     * Provide all Objects of the specified dataTypeName.
     * 
     * @param dataTypeName to be used
     * @return SearchResult with list of elements
     */
    public @Nonnull SearchResult<SearchHit> doReadAllJsonData(String dataTypeName);

    /**
     * Provide all Objects that are covered by query.
     * 
     * @param dataTypeName to be used
     * @param queryBuilder with the query to be used.
     * @return SearchResult with list of elements
     */
    public @Nonnull SearchResult<SearchHit> doReadByQueryJsonData(String dataTypeName, QueryBuilder queryBuilder);

    /**
     * Write one object into Database
     *
     * @param esId Database index
     * @param dataTypeName Name of datatype
     * @param json String in JSON format.
     * @return esId String of the database object or null in case of write problems.
     */
    public @Nullable String doWriteJsonString(String dataTypeName, @Nonnull IsEsObject esId, String json);

    /**
     * Write one object into Database
     * 
     * @param dataTypeName Name of datatype
     * @param esId of object to be replaced or null for new entry.
     * @param json String in JSON format.
     * @return esId String of the database object or null in case of write problems.
     */
    public @Nullable String doWriteRaw(String dataTypeName, @Nullable String esId, String json);

    /**
     * Write one object into Database
     * 
     * @param indexName Name of index
     * @param dataTypeName Name of datatype
     * @param esId of object to be replaced or null for new entry.
     * @param json String in JSON format.
     * @return esId String of the database object or null in case of write problems.
     */
    public @Nullable String doWriteRaw(String indexName, String dataTypeName, @Nullable String esId, String json);

    /**
     * Write one object into Database
     * 
     * @param indexName Name of index
     * @param dataTypeName Name of datatype
     * @param esId of object to be replaced or null for new entry.
     * @param json String in JSON format.
     * @param syncAfterRewrite trigger ES to sync after insert data
     * @return esId String of the database object or null in case of write problems.
     */
    public @Nullable String doWriteRaw(String indexName, String dataTypeName, String esId, String json,
            boolean syncAfterWrite);

    /**
     * Write one object into Database
     * 
     * @param dataTypeName Name of datatype
     * @param esId of object to be replaced or null for new entry.
     * @param json String in JSON format.
     * @param syncAfterRewrite trigger ES to sync after insert data
     * @return esId String of the database object or null in case of write problems.
     */
    public @Nullable String doWriteRaw(String dataTypeName, String esId, String json, boolean syncAfterWrite);

    /**
     * Remove Object from database
     * 
     * @param dataTypeName of object
     * @param esId of object to be deleted
     * @return success
     */
    public boolean doRemove(String dataTypeName, IsEsObject esId);

    /**
     * Remove Object from database
     * 
     * @param dataTypeName of object
     * @param esId as String of object to be deleted
     * @return success
     */
    boolean doRemove(String dataTypeName, String esId);

    /**
     * Verify if index already created
     * 
     * @param dataTypeName to be verified.
     * @return boolean accordingly
     */
    public boolean isExistsIndex(String dataTypeName);

    /**
     * Update one object in Database with id=esId or create if not exists.
     * 
     * @param dataTypeName Name of datatype
     * @param esId of object to be replaced or null for new entry.
     * @param json String in JSON format.
     * @return esId String of the database object or null in case of write problems.
     */
    public String doUpdateOrCreate(String dataTypeName, String esId, String json);

    /**
     * Update one object in Database with id=esId or create if not exists.
     * 
     * @param dataTypeName Name of datatype
     * @param esId to use for DB object
     * @param json object to write
     * @param doNotUpdateField Fields that are not updated, but inserted if DB Object not existing in DB
     * @return esId as String or null of not successfully
     */
    public String doUpdateOrCreate(String dataTypeName, String esId, String json, List<String> doNotUpdateField);

    /**
     * remove items from database by query
     * 
     * @param dataTypeName Name of datatype
     * @param query query to select items to remove
     * @return count of removed items
     */
    public int doRemove(String dataTypeName, QueryBuilder query);

    /**
     * update object in database
     * 
     * @param dataTypeName Name of datatype
     * @param json dataobject
     * @param query query to select item to update
     * @return esId which was updated or null if failed
     */
    public boolean doUpdate(String dataTypeName, String json, QueryBuilder query);

    /**
     * 
     * @param dataTypeName Name of datatype
     * @param queryBuilder query to select items to read
     * @param ignoreException flag if serverside exception will be thrown if query is not valid (needed for user entered
     *        filters)
     * @return results
     */
    SearchResult<SearchHit> doReadByQueryJsonData(String dataTypeName, QueryBuilder queryBuilder,
            boolean ignoreException);


    /**
     * read all data
     * 
     * @param dataTypeName Name of datatype
     * @param ignoreException flag if serverside exception will be thrown if query is not valid (needed for user entered
     *        filters)
     * @return results
     */
    SearchResult<SearchHit> doReadAllJsonData(String dataTypeName, boolean ignoreException);

    /**
     * @param alias
     * @param dataTypeName
     * @param queryBuilder
     * @param ignoreException
     * @return
     */
    SearchResult<SearchHit> doReadByQueryJsonData(String alias, String dataTypeName, QueryBuilder queryBuilder,
            boolean ignoreException);



}

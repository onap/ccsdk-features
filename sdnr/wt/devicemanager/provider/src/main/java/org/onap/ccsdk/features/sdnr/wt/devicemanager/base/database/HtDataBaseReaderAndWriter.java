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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic class to write lists of model classes to the database.
 *
 */
public class HtDataBaseReaderAndWriter<T extends IsEsObject> {

    private static final Logger log = LoggerFactory.getLogger(HtDataBaseReaderAndWriter.class);


    private final HtDataBase db;
    private final String dataTypeName;
    private final HtMapper<T> mapper;

    /**
     * Class specific access to database
     * @param db ES database descriptor
     * @param dataTypeName datatype name
     * @param clazz class of datatype
     */
    public HtDataBaseReaderAndWriter(HtDataBase db, String dataTypeName, Class<? extends T> clazz) {

        this.db = db;
        this.dataTypeName = dataTypeName;
        this.mapper = new HtMapper<>( clazz );

    }
    /**
     * @return dataTypeName
     */
    public String getDataTypeName() {
        return this.dataTypeName;
    }
    /**
     * Remove Object from database
     * @param object Object with content
     * @return true if remove is done
     */
    public boolean doRemove( T object) {

        return db.doRemove(dataTypeName, object );

    }

    /**
     * Remove all data that match the filter
     * @param query to specify data to be deleted
     * @return number of removed objects
     */
    public int doRemoveByQuery(QueryBuilder query) {

        int idx = 0;                //Idx for getAll
        int iterateLength = 100;    //Step width for iterate

        SearchHit hits[];
        do {
            hits = db.doReadByQueryJsonData(idx, iterateLength, dataTypeName, query);
            log.debug("Found: {} elements: {}  Failures: {}",dataTypeName,hits.length, mapper.getMappingFailures());

            T object;
            idx += hits.length;
            for (SearchHit hit : hits) {

                object = mapper.getObjectFromJson( hit.getSourceRef() );

                log.debug("Mapp Object: {}\nSource: '{}'\nResult: '{}'\n Failures: {}", hit.getId(), hit.getSourceAsString(), object, mapper.getMappingFailures());
                if (object != null) {
                    object.setEsId( hit.getId() );
                    doRemove(object);
                } else {
                    log.warn("Mapp result null Object: {}\n Source: '{}'\n : '", hit.getId(), hit.getSourceAsString());
                }
            }
        } while (hits.length == iterateLength); //Do it until end indicated, because less hits than iterateLength allows.

        return idx;
    }

    /**
     * Do the mapping for test purpose
     * @param object object for test purpose
     * @return json String
     */
    public String getJson( T object ) {
        String json = mapper.objectToJson(object);
        return json;
    }

    /**
     * Write one object into Database
     * @param object Object with content
     * @return This object for chained call pattern.
     */
    public T doWrite( T object) {

        String json = mapper.objectToJson(object);
        return doWrite(object, json);

    }

    /**
     * Write one object into Database
     * @param object Object with content
     * @param json string
     * @return This object for chained call pattern.
     */
    public T doWrite( T object, String json) {

        log.debug("doWrite {} {}",object.getClass().getSimpleName(), object.getEsId());

        if (json != null) {
            String esId = db.doWriteJsonString(dataTypeName, object, json);
            object.setEsId(esId);
            log.debug("doWrite done for {} {}",object.getClass().getSimpleName(), object.getEsId());
            return esId == null ? null : object;
        } else {
            log.warn("Can not map object and write to database. {} {}",object.getClass().getSimpleName(), object);
            return null;
        }

    }


    /**
     * Write a list of Objects to the database.
     * @param list Object list with content
     * @return This object for chained call pattern.
     */
    public HtDataBaseReaderAndWriter<T> doWrite( Collection<T> list) {

        int writeError = 0;
        String indexName = db.getNetworkIndex();

        log.debug("Write to ES database {}, {} Class: {}  {} elements",indexName,dataTypeName, mapper.getClazz().getSimpleName(), list.size());

        if (indexName == null) {
            throw new IllegalArgumentException("Missing Index");
        }

        if (list != null && !list.isEmpty()) {
            for( T s : list ) {
                if ( doWrite(s) == null )  {
                    if ( ++writeError > 5 ) {
                        log.warn("Leave because of to >5 write errors");
                        break;
                    }
                }
            }

        }

        return this;
    }

    /**
     * Read one object via the object class specific ID
     * @param object Object refrenced by idString
     * @return The Object if found or null
     */
    public @Nullable T doRead( IsEsObject object ) {
        T res = mapper.getObjectFromJson( db.doReadJsonData( dataTypeName, object) );
        if (res != null) {
            res.setEsId(object.getEsId());
        }
        return res;
    }

    /**
     * Read one object via the object class specific ID
     * @param objectEsId Object refrence
     * @return The Object if found or null
     */
    public @Nullable T doRead( String objectEsId ) {
        T res = mapper.getObjectFromJson( db.doReadJsonData( dataTypeName, objectEsId ) );
        if (res != null) {
            res.setEsId(objectEsId);
        }
        return res;
    }
    /**
     * Get all elements of related type
     * @return all Elements
     */
    public List<T> doReadAll() {
        return doReadAll(null);
    }
    /**
     * Read all existing objects of a type
     * @param query for the elements
     * @return the list of all objects
     */
    public List<T> doReadAll(QueryBuilder query) {

        List<T> res = new ArrayList<>();
        int idx = 0;                //Idx for getAll
        int iterateLength = 100;    //Step width for iterate

        SearchHit hits[];
        do {
            if(query!=null) {
                log.trace("read data in {} {} with query {}",db.getNetworkIndex(),dataTypeName,query);
                hits=db.doReadByQueryJsonData(0, 99999, dataTypeName, query);
            }
            else {
                hits = db.doReadAllJsonData(idx, iterateLength, dataTypeName);
            }
            log.debug("Read: {} elements: {}  Failures: {}",dataTypeName,hits.length, mapper.getMappingFailures());

            T object;
            idx += hits.length;
            for (SearchHit hit : hits) {

                object = mapper.getObjectFromJson( hit.getSourceRef() );

                log.debug("Mapp Object: {}\nSource: '{}'\nResult: '{}'\n Failures: {}", hit.getId(), hit.getSourceAsString(), object, mapper.getMappingFailures());
                if (object != null) {
                    object.setEsId( hit.getId() );
                    res.add( object );
                } else {
                    log.warn("Mapp result null Object: {}\n Source: '{}'\n : '", hit.getId(), hit.getSourceAsString());
                }
            }
        } while (hits.length == iterateLength); //Do it until end indicated, because less hits than iterateLength allows.

        return res;
    }

}

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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.database.DatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper2;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to rw yang-tool generated objects into elasticsearch database. For "ES _id" exchange the esIdAddAtributteName
 * is used. This attribute mast be of type String and contains for read and write operations the object id. The function
 * can be used without id handling. If id handling is required the parameter needs to be specified by class definition
 * in yang and setting the name by using setAttributeName()
 *
 * Due to using Jackson base interfaces the org.eclipse.jdt.annotation.NonNull needs to be used here to get rid of
 * warnings
 *
 * @param <T> Yang tools generated class object.
 */
@Deprecated
public class EsDataObjectReaderWriter2<T extends DataObject> {

    private static final Logger LOG = LoggerFactory.getLogger(EsDataObjectReaderWriter2.class);

    /** Typename for elastic search data schema **/
    private String dataTypeName;

    /** Elasticsearch Database client to be used **/
    protected DatabaseClient db;

    /** Mapper with configuration to use opendaylight yang-tools builder pattern for object creation **/
    private YangToolsMapper2<T> yangtoolsMapper;

    /** Class of T as attribute to allow JSON to Class object mapping **/
    private Class<T> clazz;

    /** Field is used to write id. If null no id handling **/
    private @Nullable Field field;

    /** Attribute that is used as id field for the database object **/
    private @Nullable String esIdAddAtributteName;

    /** Interface to be used for write operations. Rule for write: T extends S and **/
    private Class<? extends DataObject> writeInterfaceClazz; // == "S"
    /** Flag true to sync this attribute during write always, what is slow and false do not sync */
    private final boolean syncAfterWrite;

    protected boolean doFullsizeRequest;

    /**
     * Elasticsearch database read and write for specific class, defined by opendaylight yang-tools.
     *
     * @param <X>
     * @param <B>
     * @param db Database access client
     * @param dataTypeName typename in database schema
     * @param clazz class of type to be handled
     * @param builderClazz class to build related object if builder pattern should be used.
     * @param syncAfterWrite
     * @throws ClassNotFoundException
     */
    public <X extends T, B> EsDataObjectReaderWriter2(DatabaseClient db,
            String dataTypeName, @Nonnull Class<T> clazz, @Nullable Class<B> builderClazz, boolean syncAfterWrite)
            throws ClassNotFoundException {
        LOG.info("Create {} for datatype {} class {}", this.getClass().getName(), dataTypeName, clazz.getName());

        this.esIdAddAtributteName = null;
        this.field = null;
        this.writeInterfaceClazz = clazz;
        this.db = db;
        this.dataTypeName = dataTypeName;
        this.yangtoolsMapper = new YangToolsMapper2<>(clazz, builderClazz);
        this.clazz = clazz;
        this.syncAfterWrite = syncAfterWrite;
        this.doFullsizeRequest = false;
    }

    public void setFullsizeRequest(boolean fullsizeRequest) {
        this.doFullsizeRequest = fullsizeRequest;
    }
    public <X extends T, B> EsDataObjectReaderWriter2(DatabaseClient db,
            Entity dataTypeName, @Nonnull Class<T> clazz, @Nullable Class<B> builderClazz)
            throws ClassNotFoundException {
        this(db, dataTypeName.getName(), clazz, builderClazz, false);
    }

    public <X extends T, B> EsDataObjectReaderWriter2(DatabaseClient db,
            Entity dataTypeName, @Nonnull Class<T> clazz, @Nullable Class<B> builderClazz, boolean syncAfterWrite)
            throws ClassNotFoundException {
        this(db, dataTypeName.getName(), clazz, builderClazz, syncAfterWrite);
    }

    public EsDataObjectReaderWriter2(DatabaseClient db, Entity dataTypeName, @Nonnull Class<T> clazz,
            boolean syncAfterWrite) throws ClassNotFoundException {
        this(db, dataTypeName.getName(), clazz, null, syncAfterWrite);
    }

    public EsDataObjectReaderWriter2(DatabaseClient db, Entity dataTypeName, Class<T> clazz)
            throws ClassNotFoundException {
        this(db, dataTypeName.getName(), clazz, null, false);
    }

    /**
     * Get Datatype name
     *
     * @return string with dataTypeName
     */
    public String getDataTypeName() {
        return dataTypeName;
    }

    /**
     * Simlar to {@link #setEsIdAttributeName()}, but adapts the parameter to yangtools attribute naming schema
     *
     * @param esIdAttributeName is converted to UnderscoreCamelCase
     * @return this for further operations.
     */
    public EsDataObjectReaderWriter2<T> setEsIdAttributeNameCamelized(String esIdAttributeName) {
        return setEsIdAttributeName(YangToolsMapperHelper.toCamelCaseAttributeName(esIdAttributeName));
    }

    /**
     * Attribute name of class that is containing the object id
     *
     * @param esIdAttributeName of the implementation class for the yangtools interface. Expected attribute name format
     *        is CamelCase with leading underline. @
     * @return this for further operations.
     * @throws SecurityException if no access or IllegalArgumentException if wrong type or no attribute with this name.
     */
    public <B> EsDataObjectReaderWriter2<T> setEsIdAttributeName(String esIdAttributeName) {
        LOG.debug("Set attribute '{}'", esIdAttributeName);
        this.esIdAddAtributteName = null; // Reset status
        this.field = null;

        Field attributeField;
        try {
            B builder = yangtoolsMapper.getBuilder(clazz);
            if (builder == null) {
                String msg = "No builder for " + clazz;
                LOG.debug(msg);
                throw new IllegalArgumentException(msg);
            } else {
                T object = YangToolsMapperHelper.callBuild(builder);
                attributeField = object.getClass().getDeclaredField(esIdAttributeName);
                if (attributeField.getType().equals(String.class)) {
                    attributeField.setAccessible(true);
                    this.esIdAddAtributteName = esIdAttributeName; // Set new status if everything OK
                    this.field = attributeField;
                } else {
                    String msg = "Wrong field type " + attributeField.getType().getName() + " of " + esIdAttributeName;
                    LOG.debug(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        } catch (NoSuchFieldException e) {
            // Convert to run-time exception
            String msg = "NoSuchFieldException for '" + esIdAttributeName + "' in class " + clazz.getName();
            LOG.debug(msg);
            throw new IllegalArgumentException(msg);
        } catch (SecurityException e) {
            LOG.debug("Access problem " + esIdAttributeName, e);
            throw e;
        } catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return this;
    }

    /**
     * Specify subclass of T for write operations.
     *
     * @param writeInterfaceClazz
     */
    public EsDataObjectReaderWriter2<T> setWriteInterface(Class<? extends DataObject> writeInterfaceClazz) {
        LOG.debug("Set write interface to {}", writeInterfaceClazz);
        if (writeInterfaceClazz == null) {
            throw new IllegalArgumentException("Null not allowed here.");
        }

        this.writeInterfaceClazz = writeInterfaceClazz;
        return this;
    }

    public interface IdGetter<S extends DataObject> {
        String getId(S object);
    }

    public <S extends DataObject> void write(List<S> objectList, IdGetter<S> idGetter) {
        for (S object : objectList) {
            write(object, idGetter.getId(object));
        }
    }

    /**
     * Write child object to database with specific id
     *
     * @param object to be written
     * @param esId use the id or if null generate unique id
     * @return String with id or null
     */
    public @Nullable <S extends DataObject> String write(S object, @Nullable String esId) {
        if (object != null && writeInterfaceClazz.isInstance(object)) {
            try {
                String json = yangtoolsMapper.writeValueAsString(object);
                return db.doWriteRaw(dataTypeName, esId, json, this.syncAfterWrite);
            } catch (JsonProcessingException e) {
                LOG.error("Write problem: ", e);
            }
        } else {
            LOG.error("Type {} does not provide interface {}", object != null ? object.getClass().getName() : "null",
                    writeInterfaceClazz.getName());
        }
        return null;
    }

    /**
     * Update partial child object to database with match/term query
     *
     * @param <S> of object
     * @param object to write
     * @param query for write of specific attributes
     * @return json string with new Object
     */
    public @Nullable <S extends DataObject> boolean update(S object, QueryBuilder query) {
        if (object != null && writeInterfaceClazz.isInstance(object)) {
            try {
                String json = yangtoolsMapper.writeValueAsString(object);
                return db.doUpdate(this.dataTypeName, json, query);
            } catch (JsonProcessingException e) {
                LOG.error("Update problem: ", e);
            }
        } else {
            LOG.error("Type {} does not provide interface {}", object != null ? object.getClass().getName() : "null",
                    writeInterfaceClazz.getName());
        }
        return false;
    }

    /**
     * Write/ update partial child object to database with specific id Write if not exists, else update
     *
     * @param object
     * @param esId
     * @return String with esId or null
     */
    public @Nullable <S extends DataObject> String update(S object, String esId) {
        return this.updateOrCreate(object, esId, null);
    }

    /**
     * See {@link doUpdateOrCreate(String dataTypeName, String esId, String json, List<String> doNotUpdateField) }
     */
    public @Nullable <S extends DataObject> String updateOrCreate(S object, String esId, List<String> onlyForInsert) {
        if (object != null && writeInterfaceClazz.isInstance(object)) {
            try {
                String json = yangtoolsMapper.writeValueAsString(object);
                return db.doUpdateOrCreate(dataTypeName, esId, json, onlyForInsert);
            } catch (JsonProcessingException e) {
                LOG.error("Update problem: ", e);
            }
        } else {
            LOG.error("Type {} does not provide interface {}", object != null ? object.getClass().getName() : "null",
                    writeInterfaceClazz.getName());
        }
        return null;
    }

    /**
     * Read object from database, by using the id field
     *
     * @param object
     * @return
     */
    public @Nullable T read(String esId) {
        @Nullable
        T res = null;
        if (esId != null) {
            String json = db.doReadJsonData(dataTypeName, esId);
            if (json != null) {
                try {
                    res = yangtoolsMapper.readValue(json.getBytes(), clazz);
                } catch (IOException e) {
                    LOG.error("Problem: ", e);
                }
            } else {
                LOG.debug("Can not read from DB id {} type {}", esId, dataTypeName);
            }
        }
        return res;
    }

    /**
     * Remove object
     *
     * @param esId to identify the object.
     * @return success
     */
    public boolean remove(String esId) {
        return db.doRemove(this.dataTypeName, esId);
    }

    public int remove(QueryBuilder query) {
        return this.db.doRemove(this.dataTypeName, query);
    }

    /**
     * Get all elements of related type
     *
     * @return all Elements
     */
    public SearchResult<T> doReadAll() {
        return doReadAll(null);
    }

    public SearchResult<T> doReadAll(QueryBuilder query) {
        return this.doReadAll(query, false);
    }

    /**
     * Read all existing objects of a type
     *
     * @param query for the elements
     * @return the list of all objects
     */

    public SearchResult<T> doReadAll(QueryBuilder query, boolean ignoreException) {

        if(this.doFullsizeRequest) {
            query.doFullsizeRequest();
        }
        SearchResult<T> res = new SearchResult<>();
        SearchResult<SearchHit> result;
        List<SearchHit> hits;
        if (query != null) {
            LOG.debug("read data in {} with query {}", dataTypeName, query.toJSON());
            result = db.doReadByQueryJsonData(dataTypeName, query, ignoreException);
        } else {
            result = db.doReadAllJsonData(dataTypeName, ignoreException);
        }
        hits = result.getHits();
        LOG.debug("Read: {} elements: {}", dataTypeName, hits.size());

        T object;
        for (SearchHit hit : hits) {
            object = getT(hit.getSourceAsString());
            LOG.debug("Mapp Object: {}\nSource: '{}'\nResult: '{}'\n", hit.getId(),
                    hit.getSourceAsString(), object);
            if (object != null) {
                setEsId(object, hit.getId());
                res.add(object);
            } else {
                LOG.warn("Mapp result null Object: {}\n Source: '{}'\n : '", hit.getId(), hit.getSourceAsString());
            }
        }
        res.setTotal(result.getTotal());
        return res;
    }

    /* ---------------------------------------------
     * Private functions
     */

    private void setEsId(T object, String esId) {
        if (field != null) {
            try {
                field.set(object, esId);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOG.debug("Field set problem.", e);
            }
        }
    }

    private @Nullable T getT(String jsonString) {
        try {
            return yangtoolsMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            LOG.info("Mapping problem {}:", clazz.getName(), e);
            return null;
        }
    }

}

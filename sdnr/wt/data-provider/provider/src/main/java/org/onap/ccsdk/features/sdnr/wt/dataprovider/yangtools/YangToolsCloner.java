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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangToolsCloner {

    private static YangToolsMapper yangtoolsMapper = new YangToolsMapper();
    private static final Logger LOG = LoggerFactory.getLogger(YangToolsCloner.class);
    public static final int ACCESSOR_FIELD = 0;
    public static final int ACCESSOR_METHOD = 1;


    private final int accessor;

    private YangToolsCloner(int ac) {
        this.accessor = ac;
    }

    public static YangToolsCloner instance() {
        return instance(ACCESSOR_METHOD);
    }

    public static YangToolsCloner instance(int ac) {
        return new YangToolsCloner(ac);
    }

    /**
     *
     * @param source source object
     * @param clazz Class of return object
     * @return list of cloned object
     * @return
     */
    public <S extends DataObject, T extends DataObject> List<T> cloneList(List<S> source, Class<T> clazz) {
        return cloneList(source, clazz, null);
    }

    /**
     *
     * @param source source object
     * @param clazz Class of return object
     * @attrList filter for attribute Names to clone
     * @return list of cloned object
     */
    public <S extends DataObject, T extends DataObject> List<T> cloneList(List<S> source, Class<T> clazz,
            @Nullable List<String> attrList) {
        if (source == null) {
            return null;
        }
        List<T> list = new ArrayList<>();
        for (S s : source) {
            list.add(clone(s, clazz, attrList));
        }
        return list;
    }

    /**
     *
     * @param source source object
     * @param clazz Class of return object
     * @return cloned object
     */
    public <S, T extends DataObject> T clone(S source, Class<T> clazz) {
        return clone(source, clazz, null);
    }

    /**
     *
     * @param source source object
     * @param clazz Class of return object
     * @attrList if empty copy all else list of attribute Names to clone
     * @return cloned object
     */
    public <S, T extends DataObject> T clone(S source, Class<T> clazz, @Nullable List<String> attrList) {
        if (source == null) {
            return (T) null;
        }
        Field[] attributeFields;
        Field sourceField;
        Method m;
        Builder<T> builder = yangtoolsMapper.getBuilder(clazz);
        T object = builder.build();
        attributeFields = object.getClass().getDeclaredFields();
        for (Field attributeField : attributeFields) {
            // check if attr is in inclusion list
            if (attrList != null && !attrList.contains(attributeField.getName())) {
                continue;
            }
            // ignore QNAME
            if (attributeField.getName().equals("QNAME")) {
                continue;
            }

            attributeField.setAccessible(true);
            try {
                if (accessor == ACCESSOR_FIELD) {
                    sourceField = source.getClass().getDeclaredField(attributeField.getName());
                    sourceField.setAccessible(true);
                    if (attributeField.getType().equals(String.class) && !sourceField.getType().equals(String.class)) {
                        attributeField.set(object, String.valueOf(sourceField.get(source)));
                    } else {
                        attributeField.set(object, sourceField.get(source));
                    }
                } else if (accessor == ACCESSOR_METHOD) {
                    String getter = getter(attributeField.getName());
                    System.out.println("getter=" + getter);
                    m = source.getClass().getDeclaredMethod(getter);
                    m.setAccessible(true);
                    if (attributeField.getType().equals(String.class) && !m.getReturnType().equals(String.class)) {
                        attributeField.set(object, String.valueOf(m.invoke(source)));
                    } else {
                        attributeField.set(object, m.invoke(source));
                    }
                }

            } catch (NoSuchMethodException | NoSuchFieldException e) {
                // Convert to run-time exception
                String msg = "no such field " + attributeField.getName() + " in class " + source.getClass().getName();
                LOG.debug(msg);
                // throw new IllegalArgumentException(msg);
            } catch (IllegalAccessException | SecurityException e) {
                LOG.debug("Access problem " + attributeField.getName(), e);
            } catch (IllegalArgumentException e) {
                LOG.debug("argument problem " + attributeField.getName(), e);
            } catch (InvocationTargetException e) {
                LOG.debug("invocation problem " + attributeField.getName(), e);
            }
        }

        return object;
    }

    private static String getter(String name) {
        return String.format("%s%s%s", "get", name.substring(1, 2).toUpperCase(), name.substring(2));
    }

    public <S extends DataObject, T extends DataObject, B extends Builder<T>> B cloneToBuilder(S source, B builder) {
        return cloneToBuilder(source, builder, null);
    }

    public <S extends DataObject, T extends DataObject, B extends Builder<T>> B cloneToBuilder(S source, B builder,
            @Nullable List<String> attrList) {
        Field[] attributeFields;
        Field sourceField;
        Method m;
        attributeFields = builder.getClass().getDeclaredFields();
        for (Field attributeField : attributeFields) {
            // check if attr is in inclusion list
            if (attrList != null && !attrList.contains(attributeField.getName())) {
                continue;
            }
            // ignore QNAME
            if (attributeField.getName().equals("QNAME")) {
                continue;
            }

            attributeField.setAccessible(true);
            try {
                if (accessor == ACCESSOR_FIELD) {
                    sourceField = source.getClass().getDeclaredField(attributeField.getName());
                    sourceField.setAccessible(true);
                    if (attributeField.getType().equals(String.class) && !sourceField.getType().equals(String.class)) {
                        attributeField.set(builder, String.valueOf(sourceField.get(source)));
                    } else {
                        attributeField.set(builder, sourceField.get(source));
                    }
                } else if (accessor == ACCESSOR_METHOD) {
                    m = source.getClass().getDeclaredMethod(getter(attributeField.getName()));
                    m.setAccessible(true);
                    if (attributeField.getType().equals(String.class) && !m.getReturnType().equals(String.class)) {
                        attributeField.set(builder, String.valueOf(m.invoke(source)));
                    } else {
                        attributeField.set(builder, m.invoke(source));
                    }
                }

            } catch (NoSuchMethodException | NoSuchFieldException e) {
                // Convert to run-time exception
                String msg = "no such field " + attributeField.getName() + " in class " + source.getClass().getName();
                LOG.debug(msg);
                // throw new IllegalArgumentException(msg);
            } catch (IllegalAccessException | SecurityException e) {
                LOG.debug("Access problem " + attributeField.getName(), e);
            } catch (IllegalArgumentException e) {
                LOG.debug("argument problem " + attributeField.getName(), e);
            } catch (InvocationTargetException e) {
                LOG.debug("invocation problem " + attributeField.getName(), e);
            }
        }
        return builder;
    }
}

/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.yang.mapper;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.google.common.collect.Maps;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.binding.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangToolsMapperHelper {

    private static final Logger LOG = LoggerFactory.getLogger(YangToolsMapperHelper.class);
    private static final String TYPEOBJECT_INSTANCE_METHOD = "getDefaultInstance";
    private static final String BUILDER = "Builder";
    private static final DateTimeFormatter formatterOutput =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S'Z'").withZone(ZoneOffset.UTC);

    private static BundleContext context = getBundleContext();
    private static ConcurrentHashMap<String, Class<?>> cache = new ConcurrentHashMap<>();

    private YangToolsMapperHelper() {
        //Make unaccessible
    }

    public static Class<?> findClass(String name) throws ClassNotFoundException {

        //Try first in cache
        Class<?> res = cache.get(name);
        if (res != null) {
            return res;
        }
        //Try first in actual bundle
        try {
            return loadClass(null, name);
        } catch (ClassNotFoundException e) {
            // No problem, this bundle doesn't have the class
        }
        // Try to find in other bundles
        if (context != null) {
            //OSGi environment
            for (Bundle b : context.getBundles()) {
                try {
                    return loadClass(b, name);
                } catch (ClassNotFoundException e) {
                    // No problem, this bundle doesn't have the class
                }
            }
        }
        // really not found in any bundle
        throw new ClassNotFoundException("Can not find class '" + name + "'");
    }

    private static Class<?> loadClass(Bundle b, String name) throws ClassNotFoundException {
        Class<?> res = b == null ? Class.forName(name) : b.loadClass(name);
        cache.put(name, res);
        return res;
    }

    /**
     * Verify if builder is available
     *
     * @throws ClassNotFoundException
     **/
    public static Class<?> assertBuilderClass(Class<?> clazz) throws ClassNotFoundException {
        return getBuilderClass(getBuilderClassName(clazz));
    }

    public static Class<?> getBuilderClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    public static Class<?> getBuilderClass(Class<?> clazz) throws ClassNotFoundException {
        return findClass(getBuilderClassName(clazz));
    }

    /**
     * Create name of builder class
     *
     * @param <T>
     * @param clazz
     * @return builders class name
     * @throws ClassNotFoundException
     */
    public static String getBuilderClassName(Class<?> clazz) {
        return clazz.getName() + BUILDER;
    }

    @SuppressWarnings("unchecked")
    public static Class<?> findBuilderClass(DeserializationContext ctxt, Class<?> clazz)
            throws ClassNotFoundException {
        return findClass(getBuilderClassName(clazz));
    }

    public static Optional<Class<?>> findBuilderClassOptional(DeserializationContext ctxt,
            Class<?> clazz) {
        try {
            return Optional.of(findBuilderClass(ctxt, clazz));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public static <T extends BaseIdentity, S extends T> S getIdentityValueFromClass(Class<S> clazz) {
        try {
            Field valueField = clazz.getDeclaredField("VALUE");
            return (S) valueField.get(clazz);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        }
        return null;
    }

    public static boolean hasClassDeclaredMethod(Class<?> clazz, String name) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getInstanceByConstructor(Class<?> clazz, String arg)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        List<Class<?>> ctypes = getConstructorParameterTypes(clazz, String.class);
        Optional<Object> oObj;
        for (Class<?> ctype : ctypes) {
            if (ctype.equals(String.class)) {
                return Optional.of((T) clazz.getConstructor(ctype).newInstance(arg));
            } else if ((oObj = getDefaultInstance(ctype, arg)).isPresent()) {
                return Optional.of((T) clazz.getConstructor(ctype).newInstance(oObj.get()));
            } else {
                // TODO: recursive instantiation down to string constructor or
                // getDefaultInstance method
                LOG.debug("Not implemented arg:'{}' class:'{}'", arg, clazz);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getDefaultInstance(@Nullable Class<?> clazz, String arg) throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        LOG.trace("arg:'{}' clazz '{}'", arg, clazz != null ? clazz.getName() : "null");
        if (clazz != null) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method m : methods) {
                //TODO Verify argument type to avoid exception
                if (m.getName().equals(TYPEOBJECT_INSTANCE_METHOD)) {
                    Method method = clazz.getDeclaredMethod(TYPEOBJECT_INSTANCE_METHOD, String.class);
                    LOG.trace("Invoke {} available {}", TYPEOBJECT_INSTANCE_METHOD, method != null);
                    return Optional.of((T) method.invoke(null, arg));
                }
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getDefaultInstance(Optional<Class<T>> optionalClazz, String arg)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (optionalClazz.isPresent()) {
            return getDefaultInstance(optionalClazz.get(), arg);
        }
        return Optional.empty();
    }

    public static List<Class<?>> getConstructorParameterTypes(Class<?> clazz, Class<?> prefer) {

        Constructor<?>[] constructors = clazz.getConstructors();
        List<Class<?>> res = new ArrayList<>();
        for (Constructor<?> c : constructors) {
            Class<?>[] ptypes = c.getParameterTypes();
            if (ptypes.length == 1) {
                res.add(ptypes[0]);
            }

            if (prefer != null && ptypes.length == 1 && ptypes[0].equals(prefer)) {
                return Arrays.asList(prefer);
            }
        }
        return res;
    }

    public static boolean implementsInterface(Class<?> clz, Class<?> ifToImplement) {
        if (clz.equals(ifToImplement)) {
            return true;
        }
        Class<?>[] ifs = clz.getInterfaces();
        for (Class<?> iff : ifs) {
            if (iff.equals(ifToImplement)) {
                return true;
            }
        }
        return ifToImplement.isAssignableFrom(clz);
    }

    /**
     * Provide mapping of string to attribute names, generated by yang-tools. "netconf-id" converted to "_netconfId"
     *
     * @param name with attribute name, not null or empty
     * @return converted string or null if name was empty or null
     */
    public @Nullable
    static String toCamelCaseAttributeName(final String name) {
        if (name == null || name.isEmpty())
            return null;

        final StringBuilder ret = new StringBuilder(name.length());
        if (!name.startsWith("_"))
            ret.append('_');
        ret.append(toCamelCase(name));
        return ret.toString();
    }

    public static String toCamelCase(final String name) {
        int start = 0;
        final StringBuilder ret = new StringBuilder(name.length());
        for (final String word : name.split("-")) {
            if (!word.isEmpty()) {
                if (start++ == 0) {
                    ret.append(Character.toLowerCase(word.charAt(0)));
                } else {
                    ret.append(Character.toUpperCase(word.charAt(0)));
                }
                ret.append(word.substring(1));
            }
        }
        return ret.toString();
    }

    public static String toCamelCaseClassName(final String name) {
        final String clsName = toCamelCase(name);
        return clsName.substring(0, 1).toUpperCase() + clsName.substring(1);
    }

    private static BundleContext getBundleContext() {
        Bundle bundle = FrameworkUtil.getBundle(YangToolsMapperHelper.class);
        return bundle != null ? bundle.getBundleContext() : null;
    }

    public static boolean hasTime(Notification notification) {
        return notification instanceof EventInstantAware;
    }

    public static boolean hasTime(DOMNotification notification) {
        return notification instanceof DOMEvent;
    }

    public static DateAndTime getTime(Notification notification, Instant defaultValue) {
        Instant time;
        if (hasTime(notification)) { // If notification class extends/implements the EventInstantAware
            time = ((EventInstantAware) notification).eventInstant();
            LOG.debug("Event time {}", time);
        } else {
            time = defaultValue;
            LOG.debug("Defaulting to actual time of processing the notification - {}", time);
        }
        return DateAndTime.getDefaultInstance(ZonedDateTime.ofInstant(time, ZoneOffset.UTC).format(formatterOutput));
    }

    public static DateAndTime getTime(DOMNotification notification, Instant defaultValue) {
        Instant time;
        if (hasTime(notification)) { // If notification class extends/implements the EventInstantAware
            time = ((DOMEvent) notification).getEventInstant();
            LOG.debug("Event time {}", time);
        } else {
            time = defaultValue;
            LOG.debug("Defaulting to actual time of processing the notification - {}", time);
        }
        return DateAndTime.getDefaultInstance(ZonedDateTime.ofInstant(time, ZoneOffset.UTC).format(formatterOutput));
    }


    public static <K extends Identifier<V>, V extends Identifiable<K>> Map<K, V> toMap(List<V> list) {
        return list == null || list.isEmpty() ? null : Maps.uniqueIndex(list, Identifiable::key);
    }

    @SuppressWarnings("unchecked")
    public static <S, T> T callBuild(S builder)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Method method = builder.getClass().getMethod("build");
        return (T) method.invoke(builder);
    }
}

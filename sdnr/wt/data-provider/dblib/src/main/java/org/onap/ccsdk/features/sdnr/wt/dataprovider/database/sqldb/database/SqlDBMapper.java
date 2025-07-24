/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlPropertyInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.filters.DBKeyValuePair;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions.YangToolsBuilderAnnotationIntrospector;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions.YangToolsDeserializerModifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDBMapper {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDBMapper.class);

    private static final Map<Class<?>, String> mariaDBTypeMap = initTypeMap();
    private static final String ODLID_DBTYPE = "VARCHAR(40)";
    private static final String STRING_DBTYPE = "VARCHAR(255)";
    private static final String ENUM_DBTYPE = "VARCHAR(100)";
    private static final String BIGINT_DBTYPE = "BIGINT";
    public static final String ODLID_DBCOL = "controller-id";
    public static final String ID_DBCOL = "id";
    private static List<Class<?>> numericClasses = Arrays.asList(Byte.class, Integer.class, Long.class,
            BigInteger.class, Uint8.class, Uint16.class, Uint32.class, Uint64.class);
    private static final YangToolsMapper mapper = new YangToolsMapper();
    public static final String TABLENAME_CONTROLLER = "controller";
    private static final String DEFAULTID_DBTYPE = "int(11)";

    private SqlDBMapper() {

    }

    public static String createTableOdl() {
        return "CREATE TABLE IF NOT EXISTS " + TABLENAME_CONTROLLER + " (`" + ID_DBCOL + "` " + ODLID_DBTYPE + " "
                + getColumnOptions(ID_DBCOL, ODLID_DBTYPE) + "," + "`desc` " + STRING_DBTYPE + " "
                + getColumnOptions("description", STRING_DBTYPE) + "," + "primary key(" + ID_DBCOL + "))";
    }

    public static <T> String createTable(Class<T> clazz, Entity e) throws UnableToMapClassException {
        return createTable(clazz, e, "", false, true);
    }

    public static <T> String createTable(Class<T> clazz, Entity e, String suffix) throws UnableToMapClassException {
        return createTable(clazz, e, suffix, false, true);
    }

    public static <T> String createTable(Class<T> clazz, Entity e, boolean autoIndex) throws UnableToMapClassException {
        return createTable(clazz, e, "", false, true);
    }

    public static <T> String createTable(Class<T> clazz, Entity e, String suffix, boolean autoIndex,
            boolean withControllerId) throws UnableToMapClassException {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `" + e.getName() + suffix + "` (\n");
        if (autoIndex) {
            sb.append("`" + ID_DBCOL + "` " + DEFAULTID_DBTYPE + " " + getColumnOptions(ID_DBCOL, DEFAULTID_DBTYPE)
                    + ",\n");
        } else {
            sb.append("`" + ID_DBCOL + "` " + STRING_DBTYPE + " " + getColumnOptions(ID_DBCOL, STRING_DBTYPE) + ",\n");
        }
        if (withControllerId) {
            sb.append("`" + ODLID_DBCOL + "` " + ODLID_DBTYPE + " " + getColumnOptions(ODLID_DBCOL, ODLID_DBTYPE) + ",\n");
        }
        for (Method method : getFilteredMethods(clazz, true)) {
            Class<?> valueType = method.getReturnType();
            String colName = getColumnName(method);
            if (ID_DBCOL.equals(colName)) {
                continue;
            }
            String dbType = getDBType(valueType);
            String options = getColumnOptions(colName, dbType);
            sb.append("`" + colName + "` " + dbType + " " + options + ",\n");
        }
        sb.append("primary key(" + ID_DBCOL + ")");
        if (withControllerId) {
            sb.append(",foreign key(`" + ODLID_DBCOL + "`) references " + TABLENAME_CONTROLLER + "(" + ID_DBCOL + ")");
        }

        sb.append(");");
        return sb.toString();
    }

    private static String getColumnOptions(String colName, String dbType) {
        StringBuilder options = new StringBuilder();
        if (dbType.contains("VARCHAR")) {
            options.append("CHARACTER SET utf8 ");
        }
        if (ID_DBCOL.equals(colName) || ODLID_DBCOL.equals(colName)) {
            if (dbType.equals(DEFAULTID_DBTYPE)) {
                options.append("NOT NULL AUTO_INCREMENT");
            } else {
                options.append("NOT NULL");
            }
        }
        return options.toString();
    }

    /**
     *
     * @param clazz Class to scan for methods for their properties
     * @param getterOrSetter true for using only getters, false using setters
     * @return
     */
    public static List<Method> getFilteredMethods(Class<?> clazz, boolean getterOrSetter) {
        Method[] methods = clazz.getMethods();
        List<Method> list = new ArrayList<>();
        for (Method method : methods) {
            if (getterOrSetter) {
                if (!isGetter(method)) {
                    continue;
                }
            } else {
                if (!isSetter(method)) {
                    continue;
                }
            }
            if (ignoreMethod(method, methods, getterOrSetter)) {
                continue;
            }
            list.add(method);
        }
        return list;
    }


    private static Map<Class<?>, String> initTypeMap() {
        Map<Class<?>, String> map = new HashMap<>();
        map.put(String.class, STRING_DBTYPE);
        map.put(Boolean.class, "BOOLEAN");
        map.put(Byte.class, "TINYINT");
        map.put(Integer.class, "INTEGER");
        map.put(Long.class, "BIGINT");
        map.put(BigInteger.class, "BIGINT");
        map.put(Uint8.class, "SMALLINT");
        map.put(Uint16.class, "INTEGER");
        map.put(Uint32.class, BIGINT_DBTYPE);
        map.put(Uint64.class, BIGINT_DBTYPE); //????
        map.put(DateAndTime.class, "DATETIME(3)");
        return map;
    }

    private static boolean ignoreMethod(Method method, Method[] classMehtods, boolean getterOrSetter) {
        final String name = method.getName();
        if (name.equals("getAugmentations") || name.equals("getImplementedInterface")
                || name.equals("implementedInterface") || name.equals("getClass")) {
            return true;
        }
        for (Method cm : classMehtods) {
            if (!cm.equals(method) && cm.getName().equals(name)) {
                //resolve conflict
                return !resolveConflict(method, cm, getterOrSetter);
            }
            //silicon fix for deprecated is-... and getIs- methods for booleans
            if (method.getReturnType().equals(Boolean.class) && getterOrSetter && name.startsWith("get")
                    && cm.getName().startsWith("is") && cm.getName().endsWith(name.substring(3))) {
                return true;
            }
        }
        return false;
    }

    private static boolean resolveConflict(Method m1, Method m2, boolean getterOrSetter) {
        Class<?> p1 = getterOrSetter ? m1.getReturnType() : m1.getParameterTypes()[0];
        Class<?> p2 = getterOrSetter ? m2.getReturnType() : m2.getParameterTypes()[0];
        if (YangToolsBuilderAnnotationIntrospector.isAssignable(p1, p2, Map.class, List.class)) {
            return p1.isAssignableFrom(List.class); //prefer List setter
        } else if (YangToolsBuilderAnnotationIntrospector.isAssignable(p1, p2, Uint64.class, BigInteger.class)) {
            return p1.isAssignableFrom(Uint64.class);
        } else if (YangToolsBuilderAnnotationIntrospector.isAssignable(p1, p2, Uint32.class, Long.class)) {
            return p1.isAssignableFrom(Uint32.class);
        } else if (YangToolsBuilderAnnotationIntrospector.isAssignable(p1, p2, Uint16.class, Integer.class)) {
            return p1.isAssignableFrom(Uint16.class);
        } else if (YangToolsBuilderAnnotationIntrospector.isAssignable(p1, p2, Uint8.class, Short.class)) {
            return p1.isAssignableFrom(Uint8.class);
        }
        return false;
    }

    public static String getColumnName(Method method) {
        String camelName = (method.getName().startsWith("get") || method.getName().startsWith("set"))
                ? method.getName().substring(3)
                : method.getName().substring(2);
        return convertCamelToKebabCase(camelName);
    }

    public static String getDBType(Class<?> valueType) throws UnableToMapClassException {
        String type = mariaDBTypeMap.getOrDefault(valueType, null);
        if (type == null) {
            if (implementsInterface(valueType, DataObject.class) || implementsInterface(valueType, List.class)
                    || implementsInterface(valueType, Map.class) || implementsInterface(valueType, Set.class)) {
                return "JSON";
            }
            if (implementsInterface(valueType, EnumTypeObject.class)) {
                return ENUM_DBTYPE;
            }
            throw new UnableToMapClassException("no mapping for " + valueType.getName() + " found");
        }
        return type;
    }

    private static boolean implementsInterface(Class<?> valueType, Class<?> iftoImpl) {
        return iftoImpl.isAssignableFrom(valueType);
    }

    private static boolean isGetter(Method method) {
        return method.getName().startsWith("get") || method.getName().startsWith("is")
                || method.getName().startsWith("do");
    }

    private static boolean isSetter(Method method) {
        return method.getName().startsWith("set");
    }

    /**
     * @param input string in Camel Case
     * @return String in Kebab case Inspiration from KebabCaseStrategy class of com.fasterxml.jackson.databind with an
     *         additional condition to handle numbers as well Using QNAME would have been a more fool proof solution,
     *         however it can lead to performance problems due to usage of Java reflection
     */
    private static String convertCamelToKebabCase(String input) {
        if (input == null)
            return input; // garbage in, garbage out
        int length = input.length();
        if (length == 0) {
            return input;
        }

        StringBuilder result = new StringBuilder(length + (length >> 1));

        int upperCount = 0;

        for (int i = 0; i < length; ++i) {
            char ch = input.charAt(i);
            char lc = Character.toLowerCase(ch);

            if (lc == ch) { // lower-case letter means we can get new word
                // but need to check for multi-letter upper-case (acronym), where assumption
                // is that the last upper-case char is start of a new word
                if ((upperCount > 1)) {
                    // so insert hyphen before the last character now
                    result.insert(result.length() - 1, '-');
                } else if ((upperCount == 1) && Character.isDigit(ch) && i != length - 1) {
                    result.append('-');
                }
                upperCount = 0;
            } else {
                // Otherwise starts new word, unless beginning of string
                if ((upperCount == 0) && (i > 0)) {
                    result.append('-');
                }
                ++upperCount;
            }
            result.append(lc);
        }
        return result.toString();
    }

    public static List<SqlPropertyInfo> getProperties(Class<? extends DataContainer> clazz) {
        return getFilteredMethods(clazz, true).stream().map(e -> {
            try {
                return new SqlPropertyInfo(e);
            } catch (UnableToMapClassException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
    }

    public static class UnableToMapClassException extends Exception {

        private static final long serialVersionUID = 1L;

        public UnableToMapClassException(String message) {
            super(message);
        }

    }

    public static String escape(Object o) {
        return escape(o.toString());
    }

    public static String escape(String o) {
        return o.replace("'", "\\'");
    }

    public static boolean isComplex(Class<?> valueType) {
        return DataObject.class.isAssignableFrom(valueType) || List.class.isAssignableFrom(valueType)
                || Set.class.isAssignableFrom(valueType);
    }

    public static Object getNumericValue(Object value, Class<?> valueType) {
        if (valueType.equals(Byte.class) || valueType.equals(Integer.class) || valueType.equals(Long.class)) {
            return value;
        }
        if (valueType.equals(Uint8.class) || valueType.equals(Uint16.class) || valueType.equals(Uint32.class)
                || valueType.equals(Uint64.class)) {
            return ((Number) value).longValue();
        }
        return value;
    }

    public static Object bool2int(Object invoke) {
        return Boolean.TRUE.equals(invoke) ? 1 : 0;
    }

    public static boolean isBoolean(Class<?> valueType) {
        return valueType.equals(Boolean.class);
    }

    public static boolean isNumeric(Class<?> valueType) {
        return numericClasses.contains(valueType);

    }

    private static boolean isDateTime(Class<?> valueType) {
        return valueType.equals(DateAndTime.class);
    }

    private static boolean isYangEnum(Class<?> valueType) {
        return YangToolsMapperHelper.implementsInterface(valueType, EnumTypeObject.class);
    }

    public static <T extends DataContainer> List<T> read(ResultSet data, Class<T> clazz)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException,
            SecurityException, NoSuchMethodException, JsonProcessingException, SQLException {
        return read(data, clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <S, T> List<T> read(ResultSet data, Class<T> clazz, String column)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException,
            InstantiationException, SecurityException, NoSuchMethodException, JsonProcessingException {
        if (data == null) {
            return Arrays.asList();
        }
        S builder = findPOJOBuilder(clazz);
        if (builder == null && column == null) {
            throw new InstantiationException("unable to find builder for class " + clazz.getName());
        }

        List<T> list = new ArrayList<>();
        while (data.next()) {
            if (column == null) {
                Class<?> argType;
                String col;
                for (Method m : getFilteredMethods(builder.getClass(), false)) {
                    argType = m.getParameterTypes()[0];
                    col = getColumnName(m);
                    m.setAccessible(true);
                    m.invoke(builder, getValueOrDefault(data, col, argType, null));
                }
                list.add(callBuild(builder));
            } else {
                Object value = getValueOrDefault(data, column, clazz, null);
                if (value != null) {
                    list.add((T) value);
                }
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static <S, T> T callBuild(S builder) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method method = builder.getClass().getMethod("build");
        return (T) method.invoke(builder);
    }

    @SuppressWarnings("unchecked")
    private static <S, T> S findPOJOBuilder(Class<T> ac) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, SecurityException, NoSuchMethodException {
        try {
            String builder = null;

            if (ac.isInterface()) {
                String clsName = ac.getName();
                if (clsName.endsWith("Entity")) {
                    clsName = clsName.substring(0, clsName.length() - 6);
                }
                builder = clsName + "Builder";
            }
            if (builder != null) {
                Class<?> innerBuilder = YangToolsMapperHelper.findClass(builder);
                // Class<Builder<T>> builderClass = (Class<Builder<T>>) innerBuilder;
                return (S) innerBuilder.getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException e) {

        }
        return null;
    }

    private static Object getValueOrDefault(ResultSet data, String col, Class<?> dstType, Object defaultValue)
            throws SQLException, JsonMappingException, JsonProcessingException {
        if (isBoolean(dstType)) {
            return data.getBoolean(col);
        } else if (isNumeric(dstType)) {
            return getNumeric(dstType, data.getLong(col));
        } else if (String.class.equals(dstType)) {
            return data.getString(col);
        } else if (isYangEnum(dstType)) {
            return getYangEnum(data.getString(col), dstType);
        } else if (isDateTime(dstType)) {
            String v = data.getString(col);
            return v == null || v.equals("null") ? null : DateAndTime.getDefaultInstance(v.replace(" ", "T") + "Z");
        } else if (isComplex(dstType)) {
            String v = data.getString(col);

            return (v == null || v.equalsIgnoreCase("null")) ? null : mapper.readValue(v, dstType);
        }
        return defaultValue;
    }



    private static Object getYangEnum(String value, Class<?> dstType) {
        if (value == null || value.equals("null")) {
            return null;
        }
        try {
            return YangToolsDeserializerModifier.parseEnum(value, dstType);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            LOG.warn("unable to parse enum value '{}' to class {}: ", value, dstType, e);
        }
        return null;
    }

    private static Object getNumeric(Class<?> dstType, long value) {
        if (dstType.equals(Uint64.class)) {
            return Uint64.valueOf(value);
        } else if (dstType.equals(Uint32.class)) {
            return Uint32.valueOf(value);
        } else if (dstType.equals(Uint16.class)) {
            return Uint16.valueOf(value);
        } else if (dstType.equals(Uint8.class)) {
            return Uint8.valueOf(value);
        } else if (dstType.equals(Long.class)) {
            return value;
        } else if (dstType.equals(Integer.class)) {
            return (int) value;
        } else if (dstType.equals(Byte.class)) {
            return (byte) value;
        }
        return null;
    }

    public static DBKeyValuePair<String> getEscapedKeyValue(Method m, String col, Object value)
            throws JsonProcessingException {
        Class<?> valueType = m.getReturnType();
        String svalue = null;
        if (isBoolean(valueType)) {
            svalue = String.valueOf(bool2int(value));
        } else if (isNumeric(valueType)) {
            svalue = String.valueOf(getNumericValue(value, valueType));
        } else if (isDateTime(valueType)) {
            svalue = "'" + getDateTimeValue((DateAndTime) value) + "'";
        } else if (isComplex(valueType)) {
            svalue = "'" + escape(mapper.writeValueAsString(value)) + "'";
        } else {
            svalue = "'" + escape(value) + "'";
        }
        return new DBKeyValuePair<>("`" + col + "`", svalue);
    }

    private static String getDateTimeValue(DateAndTime value) {
        String s = value.getValue();
        if (s.endsWith("Z")) {
            s = s.substring(0, s.length() - 1).replace("T", " ");
        } else if (s.contains("+")) {
            s = s.substring(0, s.indexOf("+")).replace("T", " ");
        }
        return s;
    }
}

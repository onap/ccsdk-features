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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class YangHelper2 {

    static public @NonNull Uint64 getUint64(@NonNull BigInteger val) {
        return Uint64.valueOf(val);
    }

    static public @NonNull Uint64 getUint64(@NonNull Uint64 val) {
        return val;
    }

    static public @NonNull Uint32 getUint32(@NonNull Long val) {
        return Uint32.valueOf(val);
    }

    static public @NonNull Uint32 getUint32(@NonNull Uint32 val) {
        return val;
    }

    public static @NonNull Uint16 getUint16(@Nullable Integer val) {
        return Uint16.valueOf(val);
    }

    public static @NonNull Uint16 getUint16(@Nullable Uint16 val) {
        return val;
    }

    public static @NonNull Integer getInteger(@Nullable Integer val) {
        return val;
    }

    public static @NonNull Integer getInteger(@Nullable Uint16 val) {
        return val.intValue();
    }

    public static @NonNull Long getInteger(@Nullable Long val) {
        return val;
    }

    public static @NonNull Long getInteger(@Nullable Uint32 val) {
        return val.longValue();
    }
    /**
     * Aluminium version
     */
    public static <K extends Identifier<T>,T extends Identifiable<K>> Map<K, T> getListOrMap(Class<K> clazz, List<T> list) {
        Map<K,T> map = new HashMap<>();
        for(T listelement:list) {
            Constructor<K> constructor;
            try {
                constructor = clazz.getConstructor(clazz);
            clazz.getConstructors();
            map.put(constructor.newInstance(listelement.key()), listelement);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalArgumentException("Can not create map element.", e);
            }
        }
        return map;
    }
    public static <K extends Identifier<T>,T extends Identifiable<K>> Map<K, T> getListOrMap(Class<K> clazz, T listElement) {
        return getListOrMap(clazz, Arrays.asList(listElement) );
    }
    public static Uint32 getLongOrUint32(long longVal) {
        return Uint32.valueOf(longVal);
    }
    public static Uint32 getLongOrUint32(Long longVal) {
        return Uint32.valueOf(longVal);
    }
    public static Uint64 getBigIntegerOrUint64(BigInteger value) {
        return Uint64.valueOf(value);
    }
    public static Class<?> getScalarTypeObjectClass() {
        return org.opendaylight.yangtools.yang.binding.ScalarTypeObject.class;
    }

}

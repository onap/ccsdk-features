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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;

public class YangHelper {

    private YangHelper() {

    }
    /**
     * Aluminium version
     */
    public static <K extends Key<? extends EntryObject<?,K>>, V extends KeyAware<K>> Map<K, V> getListOrMap(Class<K> clazz, List<V> list) {
        Map<K,V> map = new HashMap<>();
        for(V listelement:list) {
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
    public static <K extends Key<? extends EntryObject<?,K>>, V extends KeyAware<K>> Map<K, V> getListOrMap(Class<K> clazz, V listElement) {
        return getListOrMap(clazz, Arrays.asList(listElement) );
    }
}

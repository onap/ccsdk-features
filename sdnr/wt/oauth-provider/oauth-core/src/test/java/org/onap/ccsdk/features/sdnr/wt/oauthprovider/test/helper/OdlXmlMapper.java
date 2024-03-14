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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.test.helper;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions.YangToolsBuilderAnnotationIntrospector;

public class OdlXmlMapper extends XmlMapper{

    private static final long serialVersionUID = 1L;


    public OdlXmlMapper() {
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.setSerializationInclusion(Include.NON_NULL);
        this.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        this.enable(MapperFeature.USE_GETTERS_AS_SETTERS);
        YangToolsBuilderAnnotationIntrospector introspector = new YangToolsBuilderAnnotationIntrospector();
        //introspector.addDeserializer(Main.class, ShiroMainBuilder.class.getName());

        this.setAnnotationIntrospector(introspector);
    }
}

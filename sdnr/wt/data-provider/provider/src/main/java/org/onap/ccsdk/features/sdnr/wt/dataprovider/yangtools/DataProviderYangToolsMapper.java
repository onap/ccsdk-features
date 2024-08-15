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

import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.IdentifierDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.Credentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.MeasurementKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YangToolsMapper is a specific Jackson mapper configuration for opendaylight yangtools serialization or
 * deserialization of DataObject to/from JSON TODO ChoiceIn and Credentials deserialization only for
 * LoginPasswordBuilder
 */
public class DataProviderYangToolsMapper extends YangToolsMapper {

    @SuppressWarnings("unused")
    private final Logger LOG = LoggerFactory.getLogger(DataProviderYangToolsMapper.class);
    private static final long serialVersionUID = 1L;

    public DataProviderYangToolsMapper() {
        super();
        this.addDeserializer(Credentials.class, LoginPasswordBuilder.class.getName());
        this.addKeyDeserializer(MeasurementKey.class, new IdentifierDeserializer());
    }


}

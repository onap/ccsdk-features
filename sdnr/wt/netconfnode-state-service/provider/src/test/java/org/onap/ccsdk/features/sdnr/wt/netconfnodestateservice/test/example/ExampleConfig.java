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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.example;

import java.io.IOException;
import java.util.Objects;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.DomContext;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom.DomParser;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.config.rev201208.Configuration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleConfig.class);

    // specification of YANG module
    private static final QNameModule CONFIG_MODULE = QNameModule.create(
            XMLNamespace.of("urn:ietf:params:xml:ns:yang:config"), Revision.of("2020-12-08"));
    // path to 'configuration' container (it is a root container)
    private static final YangInstanceIdentifier CONFIGURATION_PATH = YangInstanceIdentifier.builder()
            .node(QName.create(CONFIG_MODULE, "configuration"))
            .build();


    public static void exampleConfig(DomContext domContext) throws YangParserException, IOException {
        // (1) preparation of schema context with module that describes configuration (it is possible to load multiple
        // schemas into parser)
        //final YangParser parser = new YangParserFactoryImpl().createParser();
    	final YangParser parser = domContext.getYangParserFactory().createParser();
        parser.addSource(YangTextSchemaSource.forResource("/META-INF/yang/config@2020-12-08.yang"));
        final EffectiveModelContext schemaContext = parser.buildEffectiveModel();

        // (2) parsing of configuration into binding-independent format
        final NormalizationResult<?> data = DomParser.parseJsonFile("/example.json", schemaContext);

        // (3) conversion into binding-aware format (md-sal codec needs to know about path on which data is placed)
        final Configuration config = (Configuration) domContext.getBindingNormalizedNodeSerializer().fromNormalizedNode(CONFIGURATION_PATH, data.data())
                .getValue();

        // (4) printing some useful information
        LOG.info("Value of 'config1': {}", config.getConfig1());
        LOG.info("Value of 'config2': {}", config.requireConfig2());
        Objects.requireNonNull(config.getEntry()).forEach((entryKey, entry) ->
                LOG.info("Value of '{}' setting: {}", entry.getSetting(), entry.getValue()));
    }

}

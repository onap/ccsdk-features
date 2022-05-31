/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

public final class TestYangParserUtil {

	private static final FileFilter YANG_FILE_FILTER = file -> {
		final String name = file.getName();
		return name.endsWith(YangConstants.RFC6020_YANG_FILE_EXTENSION) && file.isFile();
	};

	private static final @NonNull YangParserFactory PARSER_FACTORY;

	static {
		final Iterator<@NonNull YangParserFactory> it = ServiceLoader.load(YangParserFactory.class).iterator();
		if (!it.hasNext()) {
			throw new IllegalStateException("No YangParserFactory found");
		}
		PARSER_FACTORY = it.next();
	}

	public static EffectiveModelContext parseYangFiles(final YangParserConfiguration config, final File... files) {
		return parseYangFiles(config, Arrays.asList(files));
	}

	public static EffectiveModelContext parseYangFiles(final YangParserConfiguration config,
			final Collection<File> files) {
		return parseSources(config, files.stream().map(YangTextSchemaSource::forFile).collect(Collectors.toList()));
	}

	public static EffectiveModelContext parseYangResourceDirectory(final String resourcePath) {
		return parseYangResourceDirectory(resourcePath, YangParserConfiguration.DEFAULT);
	}

	public static EffectiveModelContext parseYangResourceDirectory(final String resourcePath,
			final YangParserConfiguration config) {
		final URI directoryPath;
		try {
			directoryPath = TestYangParserUtil.class.getResource(resourcePath).toURI();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Failed to open resource " + resourcePath, e);
		}
		return parseYangFiles(config, new File(directoryPath).listFiles(YANG_FILE_FILTER));
	}

	public static EffectiveModelContext parseYangSources(final YangParserConfiguration config,
			final Set<QName> supportedFeatures, final YangTextSchemaSource... sources) {
		return parseSources(config, Arrays.asList(sources));
	}

	public static EffectiveModelContext parseSources(final YangParserConfiguration config,
			final Collection<? extends SchemaSourceRepresentation> sources) {
		final YangParser parser = PARSER_FACTORY.createParser(config);

		try {
			parser.addSources(sources);
		} catch (YangSyntaxErrorException e) {
			throw new IllegalArgumentException("Malformed source", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read a source", e);
		}

		try {
			return parser.buildEffectiveModel();
		} catch (YangParserException e) {
			throw new IllegalStateException("Failed to assemble SchemaContext", e);
		}
	}
}

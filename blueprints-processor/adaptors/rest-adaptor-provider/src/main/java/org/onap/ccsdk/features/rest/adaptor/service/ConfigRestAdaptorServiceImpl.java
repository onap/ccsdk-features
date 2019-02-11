/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.features.rest.adaptor.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.features.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.features.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.features.rest.adaptor.data.RestResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class ConfigRestAdaptorServiceImpl implements ConfigRestAdaptorService {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigRestAdaptorServiceImpl.class);
    private Map<String, String> restProperties = new ConcurrentHashMap<>();

    public ConfigRestAdaptorServiceImpl() {
        this(null);
    }

    public ConfigRestAdaptorServiceImpl(final String propertyFilePath) {
        loadProps(propertyFilePath);
        try {
            String envType = restProperties.get(ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY
                + ConfigRestAdaptorConstants.REST_ADAPTOR_ENV_TYPE);

            if (!(ConfigRestAdaptorConstants.PROPERTY_ENV_PROD.equalsIgnoreCase(envType)
                || ConfigRestAdaptorConstants.PROPERTY_ENV_SOLO.equalsIgnoreCase(envType))) {
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                Runnable task = () -> loadProps(propertyFilePath);
                executor.scheduleWithFixedDelay(task, 60, 15, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    // propertyFilePath is only specified in test case.
    private void loadProps(final String propertyFilePath) {
        Properties properties = new Properties();
        if (propertyFilePath != null) {
            // Loading Default properties
            String propertyFile =
                propertyFilePath + File.separator + ConfigRestAdaptorConstants.REST_ADAPTOR_PROPERTIES_FILE_NAME;
            doLoadFromPath(propertyFile, properties);
        } else {
            // Try to load config from dir
            final String ccsdkConfigDir =
                System.getProperty(ConfigRestAdaptorConstants.SDNC_ROOT_DIR_ENV_VAR_KEY) + File.separator
                    + ConfigRestAdaptorConstants.REST_ADAPTOR_PROPERTIES_FILE_NAME;
            try (FileInputStream in = new FileInputStream(ccsdkConfigDir)) {
                properties.load(in);
                logger.info("Loaded {} properties from file {}", properties.size(), ccsdkConfigDir);
            } catch (Exception e) {
                // Try to load config from jar
                final Bundle bundle = FrameworkUtil.getBundle(ConfigRestAdaptorServiceImpl.class);
                final BundleContext ctx = bundle.getBundleContext();
                final URL url = ctx.getBundle()
                    .getResource(ConfigRestAdaptorConstants.REST_ADAPTOR_PROPERTIES_FILE_NAME);
                doLoadFromPath(url.getPath(), properties);
            }
        }
        restProperties.putAll(properties.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));
    }

    private void doLoadFromPath(final String propertyFile, final Properties properties) {
        try (FileInputStream in = new FileInputStream(propertyFile)) {
            properties.load(in);
            logger.info("Loaded {} properties from file {}", properties.size(), propertyFile);
        } catch (Exception e) {
            logger.error("Failed to load properties for file: {} "
                + ConfigRestAdaptorConstants.REST_ADAPTOR_PROPERTIES_FILE_NAME, e);
        }
    }

    @Override
    public <T> T getResource(String serviceSelector, String path, Class<T> responseType)
        throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).getResource(path, responseType);
    }

    @Override
    public <T> T postResource(String serviceSelector, String path, Object request, Class<T> responseType)
        throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).postResource(path, request, responseType);
    }

    @Override
    public <T> T exchangeResource(String serviceSelector, String path, Object request, Class<T> responseType,
        String method) throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).exchangeResource(path, request, responseType,
            method);
    }

    @Override
    public RestResponse getResource(String serviceSelector, String path) throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).getResource(path);
    }

    @Override
    public RestResponse postResource(String serviceSelector, String path, Object request)
        throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).postResource(path, request);
    }

    @Override
    public RestResponse exchangeResource(String serviceSelector, String path, Object request, String method)
        throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).exchangeResource(path, request, method);
    }

    private ConfigRestClientServiceAdapter getRestClientAdapterBySelectorName(String serviceSelector)
        throws ConfigRestAdaptorException {
        String adoptorType = restProperties.get(ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
            + ConfigRestAdaptorConstants.SERVICE_TYPE_PROPERTY);
        if (StringUtils.isNotBlank(adoptorType)) {
            if (ConfigRestAdaptorConstants.REST_ADAPTOR_TYPE_GENERIC.equalsIgnoreCase(adoptorType)) {
                return new GenericRestClientAdapterImpl(restProperties, serviceSelector);
            } else if (ConfigRestAdaptorConstants.REST_ADAPTOR_TYPE_SSL.equalsIgnoreCase(adoptorType)) {
                return new SSLRestClientAdapterImpl(restProperties, serviceSelector);
            } else {
                throw new ConfigRestAdaptorException(
                    String.format("no implementation for rest adoptor type (%s) for the selector (%s).",
                        adoptorType, serviceSelector));
            }
        } else {
            throw new ConfigRestAdaptorException(
                String.format("couldn't get rest adoptor type for the selector (%s)", serviceSelector));
        }
    }

}

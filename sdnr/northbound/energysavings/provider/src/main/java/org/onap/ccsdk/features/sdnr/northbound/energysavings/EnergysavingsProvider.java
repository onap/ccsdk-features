/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                      reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.energysavings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Future;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.energysavings.rev150105.EnergysavingsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.energysavings.rev150105.PayloadInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.energysavings.rev150105.PayloadInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.energysavings.rev150105.PayloadOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.energysavings.rev150105.PayloadOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class EnergysavingsProvider implements EnergysavingsService {

    private static final Logger LOG = LoggerFactory.getLogger(EnergysavingsProvider.class);

    private final String appName = "EnergySavings";

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration<EnergysavingsService> serviceRegistration;

    // Locations and names of the configuration files
    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final String PROPERTIES_FILE_NAME = "sdnr-energy-savings.properties";
    private static final String PARSING_ERROR =
            "Could not create the request message to send to the server; no message will be sent";

    /*
     * Use a flag veryFirstTime to ensure that some tasks are done only once. The value is set here and
     * during initialization.
     */
    private Boolean veryFirstTime = true;

    // Parameters for the REST calls

    // to publish SDNR_TO_POLICY DMaaP topic
    private WebResource dmaapSdnrToPolicyWebResource = null;

    // to the Energy Savings server
    private WebResource energySavingsWebResource = null;

    public EnergysavingsProvider(final DataBroker dataBroker, RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(EnergysavingsService.class, this);

        LOG.debug("Initializing provider for " + appName);

        Preconditions.checkNotNull(dataBroker, "dataBroker must be set");

        // Set the initialization flag so some tasks will be done only once
        veryFirstTime = true;

        // Read parameters from the properties file in SDNC_CONFIG_DIR
        String propDir = System.getenv(SDNC_CONFIG_DIR);
        if (propDir == null) {
            LOG.error("Environment variable SDNC_CONFIG_DIR is not set");
            propDir = "/opt/onap/ccsdk/data/properties/";
        } else if (!propDir.endsWith("/")) {
            propDir = propDir + "/";
        }

        // Get the parameters for the REST calls
        HashMap<String, String> dmaapPolicyHttpParams = new HashMap<String, String>();
        HashMap<String, String> energySavingsServerHttpParams = new HashMap<String, String>();

        try (FileInputStream fileInput = new FileInputStream(propDir + PROPERTIES_FILE_NAME)) {
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();

            for (String param : new String[] {"url", "httpMethod", "authentication", "user", "password"}) {
                dmaapPolicyHttpParams.put(param, properties.getProperty("dmaapPolicy." + param));
                energySavingsServerHttpParams.put(param, properties.getProperty("energySavingsServer." + param));
            }
        } catch (FileNotFoundException e) {
            LOG.error("Unexpected value for energy savings server authentication: ");
        } catch (IOException e) {
            LOG.error("Unexpected value for energy savings server authentication: ");
        }

        // Create a web resource for the Energy Savings server
        ClientConfig esClientConfig = new DefaultClientConfig();
        // 3 minute read time out
        esClientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 180000);
        // 1 minute connect time out
        esClientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 60000);
        Client esClient = Client.create(esClientConfig);

        // Authentication for the Energy Savings server
        String authenticationMethod = energySavingsServerHttpParams.get("authentication");

        if (authenticationMethod.equals("basic")) {
            esClient.addFilter(new HTTPBasicAuthFilter(energySavingsServerHttpParams.get("user"),
                    energySavingsServerHttpParams.get("password")));
            energySavingsWebResource = esClient.resource(energySavingsServerHttpParams.get("url"));
        } else if (authenticationMethod.equals("none")) {
            energySavingsWebResource = esClient.resource(energySavingsServerHttpParams.get("url"));
        } else {
            LOG.error("Unexpected value for energy savings server authentication: " + authenticationMethod);
        }

        // Create a web resource for the DMaaP SDNR_TO_POLICY topic
        ClientConfig dmaapClientConfig = new DefaultClientConfig();
        // 3 minute read time out
        dmaapClientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 180000);
        // 1 minute connect time out
        dmaapClientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 60000);
        Client dmaapClient = Client.create(dmaapClientConfig);

        // Authentication for the DMaaP message router
        authenticationMethod = dmaapPolicyHttpParams.get("authentication");

        if (authenticationMethod.equals("basic")) {
            dmaapClient.addFilter(
                    new HTTPBasicAuthFilter(dmaapPolicyHttpParams.get("user"), dmaapPolicyHttpParams.get("password")));
            dmaapSdnrToPolicyWebResource = dmaapClient.resource(dmaapPolicyHttpParams.get("url"));
        } else if (authenticationMethod.equals("none")) {
            dmaapSdnrToPolicyWebResource = dmaapClient.resource(dmaapPolicyHttpParams.get("url"));
        } else {
            LOG.error("Unexpected value for DMaaP message router authentication: " + authenticationMethod);
        }

        LOG.debug("energySavingsServerHttpParams: " + Collections.singletonList(energySavingsServerHttpParams));
        LOG.debug("dmaapPolicyHttpParams: " + Collections.singletonList(dmaapPolicyHttpParams));
        LOG.debug("Initialization complete for " + appName);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.debug("EnergysavingsProvider Closed");
    }

    public WebResource getDmaapSdnrToPolicyWebResource() {
        return this.dmaapSdnrToPolicyWebResource;
    }

    public WebResource getEnergySavingsWebResource() {
        return this.energySavingsWebResource;
    }

    @Override
    public Future<RpcResult<PayloadOutput>> payload(PayloadInput input) {

        /*
         * Policy has published a POLICY_TO_SDNR DMaaP topic. Currently, this feature simply forwards the
         * input to the Energy Savings server untouched.
         */

        // Assume success
        Boolean requestSucceeded = true;

        // Build the result now so error messages can be included in the response
        PayloadOutputBuilder resultBuilder = new PayloadOutputBuilder();

        if (input == null) {
            LOG.error("Input is null");
            resultBuilder.setResult("Input is null");
            requestSucceeded = false;
        } else {
            try {
                PayloadInputBuilder inputBuilder = new PayloadInputBuilder(input);
                input = inputBuilder.build();
                LOG.debug("Received payload: " + input.getPayload());
            } catch (Exception e) {
                LOG.error("Cannot build input");
                resultBuilder.setResult(e.toString() + " : " + e.getMessage());
                requestSucceeded = false;
            }
        }

        /*
         * See if the web resources were created during initialization. No use in proceeding if not.
         */
        if (energySavingsWebResource == null) {
            LOG.error("energySavingsWebResouce is null");
            resultBuilder.setResult("energySavingsWebResource is null");
            requestSucceeded = false;
        }

        if (dmaapSdnrToPolicyWebResource == null) {
            LOG.error("dmaapSdnrToPolicyWebResouce is null");
            resultBuilder.setResult("dmaapSdnrToPolicyWebResource is null");
            requestSucceeded = false;
        }

        /*
         * Forward the POLICY_TO_SDNR message to the Energy Savings server
         */

        ClientResponse response = null;
        if (requestSucceeded) {
            try {
                LOG.debug("Sending message to controller: \n" + input.getPayload());
                response = energySavingsWebResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .method(HttpMethod.POST, ClientResponse.class, input.getPayload());
                LOG.debug("Received response from Energy Savings server: \n" + response.toString());
            } catch (Exception e) {
                LOG.error("Error while posting POLICY_TO_SDNR input to server:", e);
                resultBuilder.setResult("Error while posting POLICY_TO_SDNR input to server\n" + e.toString());
                requestSucceeded = false;
            }
        }

        /*
         * Return the response from the server to Policy using the SDNR_TO_POLICY topic
         */

        if (requestSucceeded) {
            String esServerResponse = response.getEntity(String.class);
            try {
                LOG.debug("Sending SDNR_TO_POLICY topic: \n" + esServerResponse);
                response =
                        dmaapSdnrToPolicyWebResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                .method(HttpMethod.POST, ClientResponse.class, esServerResponse);
                LOG.debug("Received response from DMaaP message router: \n" + response.toString());
            } catch (Exception e) {
                LOG.error("Error while posting SDNR_TO_POLICY topic: ", e);
                resultBuilder.setResult("Error while posting SDNR_TO_POLICY topic:\n" + e.toString());
                requestSucceeded = false;
            }
        }

        if (requestSucceeded) {
            return Futures.immediateFuture(
                    RpcResultBuilder.<PayloadOutput>success().withResult(resultBuilder.build()).build());
        } else {
            return Futures.immediateFuture(
                    RpcResultBuilder.<PayloadOutput>failed().withResult(resultBuilder.build()).build());
        }
    }
}

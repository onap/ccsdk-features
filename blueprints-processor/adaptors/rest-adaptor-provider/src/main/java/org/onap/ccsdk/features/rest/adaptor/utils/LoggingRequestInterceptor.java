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

package org.onap.ccsdk.features.rest.adaptor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

  private static EELFLogger logger = EELFManager.getInstance().getLogger(LoggingRequestInterceptor.class);

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    traceRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    traceResponse(response);
    return response;
  }

  @SuppressWarnings({"squid:S2629", "squid:S3457"})
  private void traceRequest(HttpRequest request, byte[] body) throws IOException {
    logger.info("===========================request begin================================================");
    logger.info("URI         : {}", request.getURI());
    logger.info("Method      : {}", request.getMethod());
    logger.info("Headers     : {}", request.getHeaders());
    logger.info("Request body: {}", new String(body, "UTF-8"));
    logger.debug("==========================request end================================================");
  }

  @SuppressWarnings({"squid:S2629", "squid:S3457"})
  private void traceResponse(ClientHttpResponse response) throws IOException {
    StringBuilder inputStringBuilder = new StringBuilder();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
    String line = bufferedReader.readLine();
    while (line != null) {
      inputStringBuilder.append(line);
      inputStringBuilder.append('\n');
      line = bufferedReader.readLine();
    }
    logger.info("============================response begin==========================================");
    logger.info("Status code  : {}", response.getStatusCode());
    logger.info("Status text  : {}", response.getStatusText());
    logger.info("Headers      : {}", response.getHeaders());
    logger.debug("Response body: {}", inputStringBuilder.toString());
    logger.debug("=======================response end=================================================");
  }

}

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

package org.onap.ccsdk.config.params.service;

import java.util.Map;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class MockComponentNode implements ComponentNode {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(MockComponentNode.class);

    @Override
    public Boolean preCondition(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.info("Received preCondition ");
        componentContext.put("test-key", "test");
        return true;
    }

    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

    }

    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.info("Received Request " + componentContext);
    }

    @Override
    public void preProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.info("Received preProcess ");

    }

    @Override
    public void postProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        logger.info("Received postProcess ");
    }

}

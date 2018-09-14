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

package org.onap.ccsdk.features.model.service;

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ComponentNodeDelegate implements SvcLogicJavaPlugin {

  private static EELFLogger logger = EELFManager.getInstance().getLogger(ComponentNodeDelegate.class);
  private ComponentNodeService componentNodeService;

  public ComponentNodeDelegate(ComponentNodeService componentNodeService) {
    logger.info("{} Constructor Initiated", "ComponentNodeDelegate");
    this.componentNodeService = componentNodeService;

  }

  public void process(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
    this.componentNodeService.process(inParams, ctx, null);
  }

}

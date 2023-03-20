/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */

const cache: { [path: string]: string } = { };
const getCapability = async (capability: string, nodeId: string, version?: string) => {
  const url = `/yang-schema/${capability}${version ? `/${version}` : ''}?node=${nodeId}`;

  const cacheHit = cache[url];
  if (cacheHit) return cacheHit;

  const res = await fetch(url);
  const yangFile = res.ok && (await res.text());
  if (yangFile !== false && yangFile !== null) {
    cache[url] = yangFile;
  }
  return yangFile;
};

export const yangService = {
  getCapability,
};
export default yangService;
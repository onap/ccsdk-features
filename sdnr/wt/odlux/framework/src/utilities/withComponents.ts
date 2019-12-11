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
import * as React from 'react';
import applicationService from '../services/applicationManager';
export type WithComponents<T extends { [name: string]: string }> = {
  components: { [prop in keyof T]: React.ComponentType }
};

export function withComponents<TProps,TMap extends { [name: string]: string }>(mapping: TMap) {
  return (component: React.ComponentType<TProps & WithComponents<TMap>>): React.ComponentType<TProps> => {
    const components = {} as any;
    Object.keys(mapping).forEach(name => {
      const [appKey, componentKey] = mapping[name].split('.');
      const reg = applicationService.applications[appKey];
      components[name] = reg && reg.exportedComponents && reg.exportedComponents[componentKey] || (() => null);
    });
    return (props: TProps) => (
      React.createElement(component, Object.assign({ components }, props))
    );
  }
}
export default withComponents;
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

export interface AvailableCapability {
  'capability-origin': string;
  capability: string;
}

export interface NetconfNodeTopologyAvailableCapabilities {
  'available-capability': AvailableCapability[];
}

export interface TopologyNode {
  'node-id': string;
  'netconf-node-topology:available-capabilities': NetconfNodeTopologyAvailableCapabilities;
}

export interface Topology {
  'topology-id': string;
  'network-topology:node': TopologyNode[];
}

/**
  * Represents the type of the features of the Module. 
  */
export interface Module {
  feature?: string[];
  location?: string[];
  name: string;
  namespace?: string;
  revision?: string;
}

export interface ModuleFeatures {
  module: Module[];
}

export interface ModuleSet {
  'module-set': ModuleFeatures[];
}

export interface FeatureTopology {
  'ietf-yang-library:yang-library' : ModuleSet; 
}

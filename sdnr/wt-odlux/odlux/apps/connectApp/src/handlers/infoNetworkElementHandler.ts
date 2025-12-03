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
import { IActionHandler } from '../../../../framework/src/flux/action';

import { AllElementInfoFeatureLoadedAction, AllElementInfoLoadedAction, LoadAllElementInfoAction } from '../actions/infoNetworkElementActions';
import { Module, TopologyNode } from '../models/topologyNetconf';
 
export interface IInfoNetworkElementsState {
  elementInfo: TopologyNode;
  busy: boolean;
}
 
export interface IInfoNetworkElementFeaturesState {
  elementFeatureInfo: Module[];
  busy: boolean;
}
 
const infoNetworkElementsStateInit: IInfoNetworkElementsState = {
  elementInfo: {
    'node-id': '',
    'netconf-node-topology:netconf-node':{
      'available-capabilities': {
      'available-capability': [],
      },
    },
  },
  busy: false,
};
 
const infoNetworkElementFeaturesStateInit: IInfoNetworkElementFeaturesState = {
  elementFeatureInfo: [],
  busy: false,
};
 
export const infoNetworkElementsActionHandler: IActionHandler<IInfoNetworkElementsState> = (state = infoNetworkElementsStateInit, action) => {
  if (action instanceof LoadAllElementInfoAction) {
    state = {
      ...state,
      busy: true,
    };
  } else if (action instanceof AllElementInfoLoadedAction) {
    if (!action.error && action.elementInfo) {
      state = {
        ...state,
        elementInfo: action.elementInfo,
        busy: false,
      };
    } else {
      state = {
        ...state,
        busy: false,
      };
    }
  }
  return state;
};
 
export const infoNetworkElementFeaturesActionHandler: IActionHandler<IInfoNetworkElementFeaturesState> = (state = infoNetworkElementFeaturesStateInit, action) => {
  if (action instanceof LoadAllElementInfoAction) {
    state = {
      ...state,
      busy: true,
    };
  } else if (action instanceof AllElementInfoFeatureLoadedAction) {
    if (!action.error && action.elementFeatureInfo) {
      state = {
        ...state,
        elementFeatureInfo: action.elementFeatureInfo,
        busy: false,
      };
    } else {
      state = {
        ...state,
        busy: false,
      };
    }
  }
  return state;
};
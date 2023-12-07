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

import { Module } from '../models/yang';
import { ViewSpecification } from '../models/uiModels';
import { IActionHandler } from '../../../../framework/src/flux/action';
import { UpdateDeviceDescription } from '../actions/deviceActions';

export interface IDeviceDescriptionState {
  nodeId: string;
  modules: {
    [name: string]: Module;
  };
  views: ViewSpecification[];
}

const deviceDescriptionStateInit: IDeviceDescriptionState = {
  nodeId: '',
  modules: {},
  views: [],
};

export const deviceDescriptionHandler: IActionHandler<IDeviceDescriptionState> = (state = deviceDescriptionStateInit, action) => {
  if (action instanceof UpdateDeviceDescription) {
    state = {
      ...state,
      nodeId: action.nodeId,
      modules: action.modules,
      views: action.views,
    };
  }
  return state;
};

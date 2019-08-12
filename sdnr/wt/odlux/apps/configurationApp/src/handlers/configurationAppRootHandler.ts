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
// main state handler

import { combineActionHandler } from '../../../../framework/src/flux/middleware';

import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { IActionHandler } from '../../../../framework/src/flux/action';

import { ViewSpecification } from '../models/uiModels';
import { CoreModelNetworkElement } from '../models/coreModel';
import { UpdateCoreModel, UpdateLoading, UpdateLp, UpdateViewData } from '../actions/configurationActions';

export interface IConfigurationAppStoreState {
  loading: boolean;
  nodeId?: string;
  lpId?: string;
  viewId?: string;
  indexValues?: string;
  capability?: string;
  conditionalPackage?: string,
  coreModel?: CoreModelNetworkElement;
  viewSpecifications: ViewSpecification[];
  viewData: {};
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    configuration: IConfigurationAppStoreState
  }
}

const configurationAppStoreStateInit: IConfigurationAppStoreState = {
  loading: false,
  viewSpecifications: [],
  viewData: {}
};

export const configurationAppRootHandler: IActionHandler<IConfigurationAppStoreState> = (state = configurationAppStoreStateInit, action) => {
  if (action instanceof UpdateLoading) {
    state = {
      ...state,
      loading: action.loading
    };
  } else if (action instanceof UpdateCoreModel) {
    state = {
      ...state,
      nodeId: action.nodeId,
      coreModel: action.coreModel,
      lpId: undefined,
      capability: undefined,
      conditionalPackage: undefined,
      viewSpecifications: [],
      viewData: { },
      indexValues: undefined,
      viewId: undefined,
    };
  } else if (action instanceof UpdateLp) {
    state = {
      ...state,
      lpId: action.lpId,
      capability: action.capability,
      conditionalPackage: action.conditionalPackage,
      viewSpecifications: action.viewSpecifications,
      viewData: { },
      indexValues: undefined,
      viewId: undefined,
    };
  } else if (action instanceof UpdateViewData) {
    state = {
      ...state,
      viewData: action.viewData,
      indexValues: action.indexValues,
      viewId: action.viewId,
    };
  }
  return state;
};

export default configurationAppRootHandler;

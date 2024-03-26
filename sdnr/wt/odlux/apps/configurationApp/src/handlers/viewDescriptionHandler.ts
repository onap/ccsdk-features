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

import { UpdateViewDescription, UpdateOutputData, UpdateNewData } from '../actions/deviceActions';
import { ViewSpecification } from '../models/uiModels';

export enum DisplayModeType {
  doNotDisplay = 0,
  displayAsObject = 1,
  displayAsList = 2,
  displayAsRPC = 3,
  displayAsMessage = 4,
}

export type DisplaySpecification =  {
  displayMode: DisplayModeType.doNotDisplay;
} | {
  displayMode: DisplayModeType.displayAsObject | DisplayModeType.displayAsList ;
  viewSpecification: ViewSpecification;
  keyProperty?: string;
  apidocPath?: string;
  dataPath?: string;
} | {
  displayMode: DisplayModeType.displayAsRPC;
  inputViewSpecification?: ViewSpecification;
  outputViewSpecification?: ViewSpecification;
  dataPath?: string;
} | {
  displayMode: DisplayModeType.displayAsMessage;
  renderMessage: string;
};

export interface IViewDescriptionState {
  vPath: string | null;
  displaySpecification: DisplaySpecification;
  newData?: any;
  viewData: any;
  outputData?: any;
}

const viewDescriptionStateInit: IViewDescriptionState = {
  vPath: null,
  displaySpecification: {
    displayMode: DisplayModeType.doNotDisplay,
  },
  viewData: null,
  outputData: undefined,
};

export const viewDescriptionHandler: IActionHandler<IViewDescriptionState> = (state = viewDescriptionStateInit, action) => {
  if (action instanceof UpdateViewDescription) {
    state = {
      ...state,
      vPath: action.vPath,
      viewData: action.viewData,
      outputData: undefined,
      displaySpecification: action.displaySpecification,
    };
  } else if (action instanceof UpdateOutputData) {
    state = {
      ...state,
      outputData: action.outputData,
    };
  } else if (action instanceof UpdateNewData) {
    state = {
      ...state,
      newData: action.newData,
    };
  }
  return state;
};

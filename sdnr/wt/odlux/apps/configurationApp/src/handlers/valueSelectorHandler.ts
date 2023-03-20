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
import { ViewSpecification } from '../models/uiModels';
import { EnableValueSelector, SetSelectedValue, UpdateDeviceDescription, SetCollectingSelectionData, UpdateViewDescription, UpdateOutputData } from '../actions/deviceActions';

export interface IValueSelectorState {
  collectingData: boolean;
  keyProperty: string | undefined;
  listSpecification: ViewSpecification | null;
  listData: any[];
  onValueSelected: (value: any) => void;
}

const dummyFunc = () => { };
const valueSelectorStateInit: IValueSelectorState = {
  collectingData: false,
  keyProperty: undefined,
  listSpecification: null,
  listData: [],
  onValueSelected: dummyFunc,
};

export const valueSelectorHandler: IActionHandler<IValueSelectorState> = (state = valueSelectorStateInit, action) => {
  if (action instanceof SetCollectingSelectionData) {
    state = {
      ...state,
      collectingData: action.busy,
    };
  } else if (action instanceof EnableValueSelector) {
    state = {
      ...state,
      collectingData: false,
      keyProperty: action.keyProperty,
      listSpecification: action.listSpecification,
      onValueSelected: action.onValueSelected,
      listData: action.listData,
    };
  } else if (action instanceof SetSelectedValue) {
    if (state.keyProperty) {
      state.onValueSelected(action.value[state.keyProperty]);
    }
    state = {
      ...state,
      collectingData: false,
      keyProperty: undefined,
      listSpecification: null,
      onValueSelected: dummyFunc,
      listData: [],
    };
  } else if (action instanceof UpdateDeviceDescription || action instanceof UpdateViewDescription || action instanceof UpdateOutputData) {
    state = {
      ...state,
      collectingData: false,
      keyProperty: undefined,
      listSpecification: null,
      onValueSelected: dummyFunc,
      listData: [],
    };
  }
  return state;
};

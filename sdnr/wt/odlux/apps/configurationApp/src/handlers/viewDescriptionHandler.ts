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

import { IActionHandler } from "../../../../framework/src/flux/action";

import { UpdatViewDescription } from "../actions/deviceActions";
import { ViewSpecification } from "../models/uiModels";

export interface IViewDescriptionState {
  vPath: string | null;
  keyProperty: string | undefined;
  displayAsList: boolean;
  viewSpecification: ViewSpecification;
  viewData: any
}

const viewDescriptionStateInit: IViewDescriptionState = {
  vPath: null,
  keyProperty: undefined,
  displayAsList: false,
  viewSpecification: {
    id: "empty",
    canEdit: false,
    parentView: "",
    name: "emplty",
    language: "en-US",
    title: "empty",
    elements: {}
  },
  viewData: null
};

export const viewDescriptionHandler: IActionHandler<IViewDescriptionState> = (state = viewDescriptionStateInit, action) => {
  if (action instanceof UpdatViewDescription) {
    state = {
      ...state,
      vPath: action.vPath,
      keyProperty: action.key,
      displayAsList: action.displayAsList,
      viewSpecification: action.view,
      viewData: action.viewData,
    }
  }
  return state;
};

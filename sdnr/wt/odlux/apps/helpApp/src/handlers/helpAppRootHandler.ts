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

import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { IActionHandler } from '../../../../framework/src/flux/action';

import { LoadTocAction, TocLoadedAction, LoadDocumentAction, DocumentLoadedAction } from '../actions/helpActions';
import { TocTreeNode } from '../models/tocNode';

export interface IHelpAppStoreState {
  busy: boolean;
  toc: TocTreeNode[] | undefined;
  content: string | undefined;
  currentPath: string | undefined;
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    help: IHelpAppStoreState
  }
}

const helpAppStoreStatcurrentPatheInit: IHelpAppStoreState = {
  busy: false,
  toc: undefined,
  content: undefined,
  currentPath: undefined
};

export const helpAppRootHandler: IActionHandler<IHelpAppStoreState> = (state = helpAppStoreStatcurrentPatheInit, action) => {
  if (action instanceof LoadTocAction) {
    state = {
      ...state,
      busy: true
    };
  } else if (action instanceof TocLoadedAction) {
    state = {
      ...state,
      busy: false,
      toc: action.toc
    };
  } else if (action instanceof LoadDocumentAction) {
    state = {
      ...state,
      busy: true
    };
  } else if (action instanceof DocumentLoadedAction) {
    state = {
      ...state,
      busy: false,
      content: action.document,
      currentPath: action.documentPath
    };
  }

  return state;
}


export default helpAppRootHandler;

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
import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';

import { TocTreeNode } from '../models/tocNode';
import helpService from '../services/helpService';

export class LoadTocAction extends Action {
  constructor() {
    super();

  }
}

export class TocLoadedAction extends Action {
  constructor(public toc?: TocTreeNode[], error?: string) {
    super();
    
  }
}

export const requestTocAsyncAction = async (dispatch: Dispatch) => {
  dispatch(new LoadTocAction);
  try {
    const toc = await helpService.getTableOfContents();
    if (toc) {
      dispatch(new TocLoadedAction(toc));
    } else {
      dispatch(new TocLoadedAction(undefined, "Could not load TOC."));
    }
  } catch (err) {
    dispatch(new TocLoadedAction(undefined, err));
  }
}

export class LoadDocumentAction extends Action {
  constructor() {
    super();

  }
}

export class DocumentLoadedAction extends Action {
  constructor(public document?: string, public documentPath?: string, error?: string) {
    super();

  }
}

export const requestDocumentAsyncActionCreator = (path: string) => async (dispatch: Dispatch) => {
  dispatch(new LoadDocumentAction);
  try {
    const doc = await helpService.getDocument(path);
    if (doc) {
      dispatch(new DocumentLoadedAction(doc, path));
    } else {
      dispatch(new DocumentLoadedAction(undefined, undefined, "Could not load document."));
    }
  } catch (err) {
    dispatch(new DocumentLoadedAction(undefined, undefined, err));
  }
}
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
import { AddErrorInfoAction } from '../../../../framework/src/actions/errorActions';

import { IAuthor } from '../models/author';
import { authorService } from '../services/authorService';

export class ApplicationBaseAction extends Action { }


export class LoadAllAuthorsAction extends ApplicationBaseAction {
  
}

// in React Action is most times a Message
export class AllAuthorsLoadedAction extends ApplicationBaseAction {
  constructor(public authors: IAuthor[] | null, public error?: string) {
    super();
  }
}

export const loadAllAuthorsAsync = (dispatch: Dispatch) => {
  dispatch(new LoadAllAuthorsAction());
  authorService.getAllAuthors().then(authors => {
    dispatch(new AllAuthorsLoadedAction(authors));
  }, error => {
    dispatch(new AllAuthorsLoadedAction(null, error));
    dispatch(new AddErrorInfoAction(error));
  });
}; 


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
import { Action, IActionHandler } from '../flux/action';
import { MiddlewareArg } from '../flux/middleware';
import { Dispatch } from '../flux/store';

import { IApplicationStoreState } from '../store/applicationStore';
import { AddErrorInfoAction, ErrorInfo } from '../actions/errorActions';

const baseUrl = `${ window.location.origin }${ window.location.pathname }`;

export class ApiAction<TResult, TSuccessAction extends Action & { result: TResult }> extends Action {
  constructor(public endpoint: string, public successAction: { new(result: TResult): TSuccessAction }, public authenticate: boolean = false) {
    super();
  }
}

export const apiMiddleware = (store: MiddlewareArg<IApplicationStoreState>) => (next: Dispatch) => <A extends Action>(action: A) => {

  // So the middleware doesn't get applied to every single action
  if (action instanceof ApiAction) {
    const user = store && store.getState().framework.authenticationState.user;
    const token = user && user.token || null;
    let config = { headers: {} };

    if (action.authenticate) {
      if (token) {
        config = {
          ...config,
          headers: {
            ...config.headers,
            // 'Authorization': `Bearer ${ token }`
            authorization: "Basic YWRtaW46YWRtaW4="
          }
        }
      } else {
        return next(new AddErrorInfoAction({ message: 'Please login to continue.' }));
      }
    }

    fetch(baseUrl + action.endpoint.replace(/\/{2,}/, '/'), config)
      .then(response =>
        response.json().then(data => ({ data, response }))
      )
      .then(result => {
        next(new action.successAction(result.data));
      })
      .catch((error: any) => {
        next(new AddErrorInfoAction((error instanceof Error) ? { error: error } : { message: error.toString() }));
      });
  }

  // let all actions pass
  return next(action);
}

export default apiMiddleware;
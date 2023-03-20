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

import { ReplaceAction } from '../actions/navigationActions';
import { AddErrorInfoAction } from '../actions/errorActions';

import { storeService } from './storeService';

const baseUri = `${ window.location.origin }`;
const absUrlPattern = /^https?:\/\//;

export const formEncode = (params: { [key: string]: string | number }) => Object.keys(params).map((key) => {
  return encodeURIComponent(key) + '=' + encodeURIComponent(params[key].toString());
}).join('&');

const wildcardToRegexp = (pattern: string) =>  {
  return new RegExp('^' + pattern.split(/\*\*/).map((p) => p.split(/\*+/).map((i) => i.replace(/[|\\{}()[\]^$+*?.]/g, '\\$&')).join('^[/]')).join('.*') + '$');
};

export const getAccessPolicyByUrl = (url: string) => {
  const result = {
    GET : false,
    POST: false,
    PUT: false,
    PATCH: false,
    DELETE: false,
  };
  
  if (!storeService.applicationStore) return result;

  const { state: { framework: { applicationState: { enablePolicy }, authenticationState: { policies } } } } = storeService.applicationStore!;
  
  result.GET = true;
  result.POST = true;
  result.PUT = true;
  result.PATCH = true;
  result.DELETE = true; 

  if (!enablePolicy || !policies || policies.length === 0) return result;

  policies.forEach(p => {
    const re = wildcardToRegexp(p.path);
    if (re.test(url)) {
      result.GET = p.methods.get != null ? p.methods.get : result.GET ;
      result.POST = p.methods.post != null ? p.methods.post : result.POST ;
      result.PUT = p.methods.put != null ? p.methods.put : result.PUT ;
      result.PATCH = p.methods.patch != null ? p.methods.patch : result.PATCH ;
      result.DELETE = p.methods.delete != null ? p.methods.delete : result.DELETE ;
    }
  }); 

  return result;

};

/** Sends a rest request to the given path. 
 * @returns The data, or null it there was any error
 */
export async function requestRest<TData>(path: string = '', init: RequestInit = {}, authenticate: boolean = true, isResource: boolean = false): Promise<TData | null | undefined> {
  const res = await requestRestExt<TData>(path, init, authenticate, isResource);
  if (res && res.status >= 200 && res.status < 300) {
    return res.data;
  }
  return null;
}

/** Sends a rest request to the given path and reports the server state. 
 *  @returns An object with the server state, a message and the data or undefined in case of a json parse error.
 */
export async function requestRestExt<TData>(path: string = '', init: RequestInit = {}, authenticate: boolean = true, isResource: boolean = false): Promise<{ status: number; message?: string; data: TData | null | undefined }> {
  const result: { status: number; message?: string; data: TData | null } = {
    status: -1,
    data: null,
  };
  const isAbsUrl = absUrlPattern.test(path);
  const uri = isAbsUrl ? path : isResource ? path.replace(/\/{2,}/i, '/') : (baseUri) + ('/' + path).replace(/\/{2,}/i, '/');
  init = {
    'method': 'GET',
    ...init,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...init.headers,
    },
  };
  if (!isAbsUrl && authenticate && storeService.applicationStore) {
    const { state: { framework: { authenticationState: { user } } } } = storeService.applicationStore;
    // do not request if the user is not valid

    if (!user || !user.isValid) {
      return {
        ...result,
        message: 'User is not valid or not logged in.',
      };
    }
    (init.headers = {
      ...init.headers,
      'Authorization': `${user.tokenType} ${user.token}`,
      //'Authorization': 'Basic YWRtaW46YWRtaW4='
    });
  }

  const fetchResult = await fetch(uri, init);

  if (fetchResult.status === 403) {
    storeService.applicationStore && storeService.applicationStore.dispatch(new AddErrorInfoAction({ title: 'Forbidden', message:'Status: [403], access denied.' }));
    return {
      ...result,
      status: 403,
      message: 'Forbidden.',
    };
  } else if (fetchResult.status === 401) {
    storeService.applicationStore && storeService.applicationStore.dispatch(new ReplaceAction(`/login?returnTo=${storeService.applicationStore.state.framework.navigationState.pathname}`));
    return {
      ...result,
      status: 401,
      message: 'Authentication requested by server.',
    };
  }
  const contentType = fetchResult.headers.get('Content-Type') || fetchResult.headers.get('content-type');
  const isJson = contentType && (contentType.toLowerCase().startsWith('application/json') || contentType.toLowerCase().startsWith('application/yang-data+json'));
  try {
    const data = (isJson ? await fetchResult.json() : await fetchResult.text()) as TData;
    return {
      ...result,
      status: fetchResult.status,
      message: fetchResult.statusText,
      data: data,
    };
  } catch (error) {
    return {
      ...result,
      status: fetchResult.status,
      message: error && error.message || String(error),
      data: undefined,
    };
  }
}
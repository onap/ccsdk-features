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
export const resolvePath = (...paths: string[]): string => {
  function resolve(pathA: string, pathB: string) {
    //  ‘a’     => ['a']
    //  'a/b'   => ['a', 'b']
    //  '/a/b'  => ['', 'a', 'b']
    //  '/a/b/' => ['', 'a', 'b', '']
    const pathBParts = pathB.split('/');
    if (pathBParts[0] === '') {
      return pathBParts.join('/');
    }
    const pathAParts = pathA.split('/');
    const aLastIndex = pathAParts.length - 1;
    if (pathAParts[aLastIndex] !== '') {
      pathAParts[aLastIndex] = '';
    }

    let part: string;
    let i = 0;
    while (typeof (part = pathBParts[i]) === 'string') {
      switch (part) {
        case '..':
          pathAParts.pop();
          pathAParts.pop();
          pathAParts.push('');
          break;
        case '.':
          pathAParts.pop();
          pathAParts.push('');
          break;
        default:
          pathAParts.pop();
          pathAParts.push(part);
          pathAParts.push('');
          break;
      }
      i++;
    }
    if (pathBParts[pathBParts.length - 1] !== '') pathAParts.pop(); 
    return pathAParts.join('/');
  }

  let i = 0;
  let path: string;
  let r = location.pathname;

  const urlRegex = /^https?\:\/\/([^\/?#]+)(?:[\/?#]|$)/i;
  const multiSlashReg = /\/\/+/g;

  while (typeof (path = paths[i]) === 'string') {
    debugger;
    const matches = path && path.match(urlRegex);
    if (matches || !i) {
      r = path;
    } else {
      path = path.replace(multiSlashReg, '/');
      r = resolve(r, path);
    }
    i++;
  }

  return r;
};
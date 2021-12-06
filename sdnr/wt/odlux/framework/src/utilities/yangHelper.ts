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


export const replaceHyphen = (name: string) => name.replace(/-([a-z])/g, (g) => (g[1].toUpperCase()));
export const replaceUpperCase = (name: string) => name.replace(/([a-z][A-Z])/g, (g) => g[0] + '-' + g[1].toLowerCase());

/***
 * Replaces whitespace with '-' and cast everything to lowercase
 */
export const toAriaLabel = (value: string) => value.replace(/\s/g, "-").toLowerCase();

export const convertPropertyNames = <T extends { [prop: string]: any }>(obj: T, conv: (name: string) => string): T => {
  return Object.keys(obj).reduce<{ [prop: string]: any }>((acc, cur) => {
    acc[conv(cur)] = typeof obj[cur] === "object" ? convertPropertyNames(obj[cur], conv) : obj[cur];
    return acc;
  }, obj instanceof Array ? [] : {}) as T;
}

export const convertPropertyValues = <T extends { [prop: string]: any }>(obj: T, conv: (name: string) => string): T => {
  return Object.keys(obj).reduce<{ [prop: string]: any }>((acc, cur) => {
    acc[cur] = typeof obj[cur] === "object"
      ? convertPropertyValues(obj[cur], conv)
      : cur === "property"
        ? conv(obj[cur])
        : obj[cur];
    return acc;
  }, obj instanceof Array ? [] : {}) as T;
}
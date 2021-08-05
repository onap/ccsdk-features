/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

export const max = <T,>(a: T[], p: (v: T) => Number) => a.reduce<T>((m, x) => p(m) > p(x) ? m : x, a[0]);
export const min = <T,>(a: T[], p: (v: T) => Number) => a.reduce<T>((m, x) => p(m) < p(x) ? m : x, a[0]);

export const isNumber = (value: string|null) =>{

    if(!value){
      return false;
    }else{
      const num = Number(value);
      return !isNaN(num);
    }
  }
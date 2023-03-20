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

import { requestRest } from "./restService";


    const settingsPath ="/userdata";


    export function getUserdata<TData>(partialPath?: string){
       let path = settingsPath;
        if(partialPath){
            path+=partialPath
        }

        const result = requestRest<TData>(path, {method: "GET"})
        return result;
    }

    export function saveUserdata<TData>(partialPath: string, data: string){

        const result = requestRest<TData>(settingsPath+partialPath, {method: "PUT", body: data})
        return result;
    }



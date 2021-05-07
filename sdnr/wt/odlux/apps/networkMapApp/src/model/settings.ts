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

export type NetworkMapSettings = { 
    networkMap: { 
        startupPosition: { latitude?: string, longitude?: string, zoom?: string }, 
        tileOpacity: string, 
        styling: { theme: string } } };

export type ThemeElement = {
    key: string, 
    site: string, 
    selectedSite: string, 
    microwaveLink: string, 
    fiberLink: string};
    
export type NetworkMapThemes = {networkMapThemes: {themes: ThemeElement[]} };

export type NetworkSettings = NetworkMapSettings & NetworkMapThemes;
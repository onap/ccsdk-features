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

export const URL_BASEPATH="lineOfSight";

export const TERRAIN_URL="/terrain"; //http://10.20.11.163:5200  /terrain

export const TILE_URL="/tiles"; //http://tile.openstreetmap.org  /tiles


export const OSM_STYLE = {
    'version': 8,
    'sources': {
        'raster-tiles': {
            'type': 'raster',
            'tiles': [
                TILE_URL+'/{z}/{x}/{y}.png'
            ],
            'tileSize': 256,
            'attribution':
                '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }
    },
    'layers': [
        {
            'id': 'simple-tiles',
            'type': 'raster',
            'source': 'raster-tiles',
            'minZoom': 0,
            'maxZoom': 18
        }
    ]
};
/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import * as mapboxgl from 'mapbox-gl';
import apartment from '../../icons/apartment.png';
import datacenter from '../../icons/datacenter.png';
import factory from '../../icons/factory.png';
import lamp from '../../icons/lamp.png';
import datacenterred from '../../icons/datacenterred.png';
import factoryred from '../../icons/factoryred.png';
import lampred from '../../icons/lampred.png';


type ImagesLoaded = (allImagesLoaded: boolean) => void;
type MapImages = {name: string, url: string}

export const Images : MapImages[]  = [
    {name: 'data-center', url: datacenter}, 
    {name: 'house', url: apartment}, 
    {name: 'factory', url: factory},
    {name: 'lamp', url: lamp},
    {name: 'data-center-red', url: datacenterred}, 
    {name: 'factory-red', url: factoryred},
    {name: 'lamp-red', url: lampred},
] ;

export const addImages = (map: mapboxgl.Map, callback?: ImagesLoaded) =>{

    Images.forEach(image => {
       
        map.loadImage(
            image.url,
            function (error: any, img: any) {
                if (error) throw error;
                map.addImage(image.name, img);
                allImagesLoaded(map, callback);
            });
    });
}

const allImagesLoaded = (map: mapboxgl.Map, callback?: ImagesLoaded) =>{

    const loadedImages = Images.map(image =>{
        return map.hasImage(image.name);
    });

    const allImagesLoaded = loadedImages.filter(el => !el);
    if(allImagesLoaded.length===0){
        callback && callback(true);
    }
}
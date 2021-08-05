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

import * as mapboxgl from "mapbox-gl";
import { LatLon } from "../model/LatLon";


export const addBaseSource = (map : mapboxgl.Map, name: string) =>{

  if(!map.getSource(name))

    map.addSource(name, {
        type: 'geojson',
        data: { type: "FeatureCollection", features: [] }
    });

}

export const addPoint = (map : mapboxgl.Map, point: LatLon) =>{
  const json = `{
    "type": "Feature",
    "properties": {},
    "geometry": {
      "type": "Point",
      "coordinates": 
        [${point.longitude}, ${point.latitude}]
      }
    }`;
    
    
      (map.getSource("route") as mapboxgl.GeoJSONSource).setData(JSON.parse(json));
}

export const addBaseLayer = (map: mapboxgl.Map, sourceName: string) =>{

  if(!map.getLayer('line'))
    map.addLayer({
        'id': 'line',
        'type': 'line',
        'source': sourceName,
        'layout': {
          'line-join': 'round',
          'line-cap': 'round'
        },
        'paint': {
          'line-color': '#88A',
          'line-width': 6,
          'line-opacity': 0.75
        }
      });

      if(!map.getLayer('points'))
      map.addLayer({
        id: 'points',
        type: 'circle',
        source: sourceName,
        paint: {
          'circle-radius': 5,
          'circle-color': '#223b53',
          'circle-stroke-color': '#225ba3',
          'circle-stroke-width': 3,
          'circle-opacity': 0.5
        }
      });
}

export const calculateDistanceInMeter = (lat1: number, lon1: number, lat2: number, lon2: number) => {
    const lonRad1 = toRad(lon1);
    const latRad1 = toRad(lat1);
    const lonRad2 = toRad(lon2);
    const latRad2 = toRad(lat2);
  
    const dLon = lonRad2 - lonRad1;
    const dLat = latRad2 - latRad1;
    const a = Math.pow(Math.sin(dLat / 2), 2) +
        Math.cos(latRad1) * Math.cos(latRad2) *
        Math.pow(Math.sin(dLon / 2), 2);
  
    const c = 2 * Math.asin(Math.sqrt(a));
  
    return 6378 * c;
  
  }
  
  function toRad(value: number) {
    return (value * Math.PI) / 180;
  }
  
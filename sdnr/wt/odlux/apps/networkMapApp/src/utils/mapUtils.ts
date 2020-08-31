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

const EARTHRADIUSM = 6378137;

type updatedCoordinates = { south: number, west: number, north: number, east: number };




export const addDistance = (south: number, west: number, north: number, east: number, distanceKm: number): updatedCoordinates => {

    const distanceInM = distanceKm * 1000;

    const dLat = distanceInM / EARTHRADIUSM;
    const dLon = distanceInM / (EARTHRADIUSM * Math.cos(Math.PI * (north + south) / 360));

    const latOffset = dLat * 180 / Math.PI;
    const lonOffset = dLon * 180 / Math.PI;

    const newEast = checkLongitude(east + lonOffset);
    const newWest = checkLongitude(west - lonOffset);
    const newNorth = checkLatitude(north + latOffset);
    const newSouth = checkLatitude(south - latOffset);

    return { east: newEast, north: newNorth, south: newSouth, west: newWest };

}


//taken from https://www.movable-type.co.uk/scripts/latlong.html
export const calculateMidPoint = (lat1: number, lon1: number, lat2: number, lon2: number) =>{

    const dLon = degrees_to_radians(lon2 - lon1);

    //convert to radians
    lat1 = degrees_to_radians(lat1);
    lat2 = degrees_to_radians(lat2);
    lon1 = degrees_to_radians(lon1);

    const Bx = Math.cos(lat2) * Math.cos(dLon);
    const By = Math.cos(lat2) * Math.sin(dLon);
    const lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
    const lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

    return [radians_to_degrees(lon3), radians_to_degrees(lat3)];
}


export const LatLonToDMS = (value:number, isLon:boolean=false) =>{
    const absoluteValue = Math.abs(value);
    const d = Math.floor(absoluteValue);
    const m = Math.floor((absoluteValue -d)* 60);
    const s = (absoluteValue - d - m / 60 ) * 3600;
    const dms=`${d}° ${m}' ${s.toFixed(2)}"`

    const sign = Math.sign(value);

    if(isLon){
        return (sign === -1 || sign === -0 ) ? dms + " W" : dms + " E";
    }else{
        return (sign === -1 || sign === -0 ) ? dms + " S" : dms + " N";
    } 
}

// Because features come from tiled vector data, feature geometries may be split
// or duplicated across tile boundaries and, as a result, features may appear
// multiple times in query results.

//taken from https://docs.mapbox.com/mapbox-gl-js/example/filter-features-within-map-view/

export const getUniqueFeatures = (array: mapboxgl.MapboxGeoJSONFeature[], comparatorProperty:string) =>{
    var existingFeatureKeys: any = {};
    
    var uniqueFeatures = array.filter(function(el) {
    if (existingFeatureKeys[el.properties![comparatorProperty]]) {
    return false;
    } else {
    existingFeatureKeys[el.properties![comparatorProperty]] = true;
    return true;
    }
    });
     
    return uniqueFeatures;
    }

const radians_to_degrees = (radians:number) =>{
    
    var pi = Math.PI;
    return radians * (180/pi);
}

 const degrees_to_radians = (degrees: number) =>
    {
    return degrees * (Math.PI/180);
    }


const checkLatitude = (lat: number) => {

    if (lat > 90)
        return 90;
    else if (lat < -90)
        return -90;
    else
        return lat;

}

const checkLongitude = (lon: number) => {
    if (lon > 180)
        return 180;
    else if (lon < -180)
        return -180;
    else
        return lon;
}




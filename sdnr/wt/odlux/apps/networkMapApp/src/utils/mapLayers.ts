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
import { Feature } from 'model/Feature';


export const addBaseLayers = (map: mapboxgl.Map, selectedPoint: Feature|null, selectedLine: Feature|null) => {
       
    const boundingBox = map.getBounds();
    map.addSource('lines', {
        type: 'geojson',
        data: { type: "FeatureCollection", features: [] }
    });

    const features = selectedLine !== null ? [selectedLine] : [];

    map.addSource('selectedLine', {
        type: 'geojson',
        data: { type: "FeatureCollection", features: features }
    });

    map.addSource('points', {
        type: 'geojson',
        data: { type: "FeatureCollection", features: [] }
    });

    const selectedPointFeature = selectedPoint !== null ? [selectedPoint] : [];


    map.addSource('selectedPoints', {
        type: 'geojson',
        data: { type: "FeatureCollection", features: selectedPointFeature }

    });

    map.addLayer({
        'id': 'lines',
        'type': 'line',
        'source': 'lines',
        'layout': {
            'line-join': 'round',
            'line-cap': 'round'
        },
        'paint': {
            'line-color': '#888',
            'line-width': 2
        }
    });

    map.addLayer({
        'id': 'selectedLine',
        'type': 'line',
        'source': 'selectedLine',
        'layout': {
            'line-join': 'round',
            'line-cap': 'round'
        },
        'paint': {
            'line-color': '#888',
            'line-width': 4
        }
    });



    map.addLayer({
        id: 'points',
        source: 'points',
        type: 'circle',
        paint: {
            'circle-color': '#11b4da',
            'circle-radius': 7,
            'circle-stroke-width': 1,
            'circle-stroke-color': '#fff'
        }
    });

    map.addLayer({
        id: 'selectedPoints',
        source: 'selectedPoints',
        type: 'circle',
        paint: {
            'circle-color': '#116bda',
            'circle-radius': 9,
            'circle-stroke-width': 1,
            'circle-stroke-color': '#fff'
        }
    });

    map.addSource("alarmedPoints", {
        type: 'geojson',
        data: {type:"FeatureCollection", features:[]}
    })

    map.addLayer({
        id: 'alarmedPoints',
        source: 'alarmedPoints',
        type: 'circle',
        paint: {
            'circle-color': '#CC0000',
            'circle-radius': 9,
            'circle-stroke-width': 1,
            'circle-stroke-color': '#fff'
        }
    });
}

export const removeBaseLayers = (map: mapboxgl.Map) => {

    map.removeLayer("points");
    map.removeLayer("lines");
    map.removeLayer('selectedPoints');
    map.removeLayer('selectedLine');

    map.removeSource("points");
    map.removeSource("lines");
    map.removeSource('selectedPoints');
    map.removeSource('selectedLine');
}

let checkedLayers = false;

const createFilter = (type:'street lamp'|'high rise building'|'data center', selectedSiteId?:string) =>{

    return selectedSiteId === undefined ? ['==', 'type', type] : ["all", ['==', 'type', type], ['!=', 'id', selectedSiteId]]
}

export const showIconLayers = (map: mapboxgl.Map, show: boolean, selectedSiteId?: string) => {

    const zoom = map.getZoom();
    
        if(show){

    if (zoom > 11) {

        const bounds = map.getBounds();

        if(map.getLayer('points')!== undefined && map.getLayer('point-lamps')===undefined && !checkedLayers){

        // if sites don't have a type don't change layers to icons
        const elements = map.queryRenderedFeatures( undefined,{
                layers: ['points'], filter:['has', 'type']
            });
            checkedLayers=true;

            if(elements.length>0 && elements.length<1000){

        if (map.getLayer('point-lamps') === undefined) {
            map.removeLayer('points');

            map.setLayoutProperty('selectedPoints', 'visibility', 'none');

            map.addLayer({
                'id': 'point-lamps',
                'type': 'symbol',
                'source': 'points',
                'layout': {
                    'icon-allow-overlap': true,
                    'icon-image': 'lamp',
                    'icon-size': 0.1

                },
                'filter': createFilter("street lamp", selectedSiteId),
            });

            map.addLayer({
                'id': 'point-building',
                'type': 'symbol',
                'source': 'points',
                'filter': createFilter("high rise building", selectedSiteId),
                'layout': {
                    'icon-allow-overlap': true,
                    'icon-image': 'house',
                    'icon-size': 0.1
                }
            });

            map.addLayer({
                'id': 'point-data-center',
                'type': 'symbol',
                'source': 'points',
                'filter': createFilter("data center", selectedSiteId),
                'layout': {
                    'icon-allow-overlap': true,
                    'icon-image': 'data-center',
                    'icon-size': 0.1
                } });

            map.addLayer({
                id: 'point-remaining',
                source: 'points',
                type: 'circle',
                'filter': ['==', 'type', ''],
                paint: {
                    'circle-color': '#11b4da',
                    'circle-radius': 7,
                    'circle-stroke-width': 1,
                    'circle-stroke-color': '#fff'
                }
            });

            map.addLayer({
                'id': 'select-point-lamps',
                'type': 'symbol',
                'source': 'selectedPoints',
                'layout': {
                    'icon-allow-overlap': true,
                    'icon-image': 'lamp',
                    'icon-size': 0.15

                },
                'filter': ['==', 'type', 'street lamp'],
            });

            map.addLayer({
                'id': 'select-point-buildings',
                'type': 'symbol',
                'source': 'selectedPoints',
                'filter': ['==', 'type', 'high rise building'],
                'layout': {
                    'icon-allow-overlap': true,
                    'icon-image': 'house',
                    'icon-size': 0.15
                }
            });

            map.addLayer({
                'id': 'select-point-data-center',
                'type': 'symbol',
                'source': 'selectedPoints',
                'filter': ['==', 'type', 'data center'],
                'layout': {
                    'icon-allow-overlap': true,
                    'icon-image': 'data-center',
                    'icon-size': 0.15
                }
            });
            }
        }
        }
       
    } else {
        swapLayersBack(map);
    }
}else{
    swapLayersBack(map);

}
    
}

export const swapLayersBack = (map: mapboxgl.Map) =>{
    checkedLayers=false;
    if (map.getLayer('points') === undefined) {

        map.setLayoutProperty('selectedPoints', 'visibility', 'visible');

        map.removeLayer('point-building');
        map.removeLayer('point-lamps');
        map.removeLayer('point-data-center');
        map.removeLayer('point-remaining');
        map.removeLayer('select-point-data-center');
        map.removeLayer('select-point-buildings');
        map.removeLayer('select-point-lamps');



        map.addLayer({
            id: 'points',
            source: 'points',
            type: 'circle',
            paint: {
                'circle-color': '#11b4da',
                'circle-radius': 7,
                'circle-stroke-width': 1,
                'circle-stroke-color': '#fff'
            }
        });

        map.moveLayer('points', map.getLayer('selectedPoints').id)


    }
}

const addClusterLayers = (map: mapboxgl.Map, data: any) => {
    map.addSource('clusters', {
        type: 'geojson',
        data: data
    });

    map.addSource('selectedLine', {
        type: 'geojson',
        data: { type: "FeatureCollection", features: [] }
    });

    map.addSource('selectedPoints', {
        type: 'geojson',
        data: { type: "FeatureCollection", features: [] }

    });

    map.addLayer({
        id: 'clusters',
        type: 'circle',
        source: 'clusters',
        filter: ['has', 'count'],
        paint: {
            'circle-color': [
                'step',
                ['get', 'count'],
                '#51bbd6',
                100,
                '#f1f075',
                750,
                '#f28cb1'
            ],
            'circle-radius': [
                'step',
                ['get', 'count'],
                20,
                100,
                30,
                750,
                40
            ]
        }
    });


    map.addLayer({
        id: 'cluster-count',
        type: 'symbol',
        source: 'clusters',
        filter: ['has', 'count'],
        layout: {
            'text-field': '{count}',
            'text-font': ['Roboto Bold'],
            'text-size': 12
        }
    });

    map.addLayer({
        'id': 'selectedLine',
        'type': 'line',
        'source': 'selectedLine',
        'layout': {
            'line-join': 'round',
            'line-cap': 'round'
        },
        'paint': {
            'line-color': '#888',
            'line-width': 4
        }
    });

    map.addLayer({
        id: 'unclustered-points',
        source: 'clusters',
        filter: ['!', ['has', 'count'],],
        type: 'circle',
        paint: {
            'circle-color': '#11b4da',
            'circle-radius': 7,
            'circle-stroke-width': 1,
            'circle-stroke-color': '#fff'
        }
    });

    map.addLayer({
        id: 'selectedPoints',
        source: 'selectedPoints',
        type: 'circle',
        paint: {
            'circle-color': '#116bda',
            'circle-radius': 9,
            'circle-stroke-width': 1,
            'circle-stroke-color': '#fff'
        }
    });

}
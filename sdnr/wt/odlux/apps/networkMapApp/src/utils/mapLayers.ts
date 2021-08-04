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
import { NetworkMapThemes, ThemeElement } from 'model/settings';

const fibreLinkColor = "#1154d9";
const microwaveLinkColor = "#039903";

class MapLayerService {

    checkedLayers = false;
    settings: NetworkMapThemes;
    selectedTheme: string | null = null;

    public addBaseSources = (map: mapboxgl.Map, selectedPoint: Feature | null, selectedLine: Feature | null) => {


        // make sure the sources don't already exist
        // (if the networkmap app gets opened quickly within short time periods, the prior sources might not be fully removed)

        if (!map.getSource("lines")) {

            map.addSource('lines', {
                type: 'geojson',
                data: { type: "FeatureCollection", features: [] }
            });
        }

        if (!map.getSource("selectedLine")) {
            const features = selectedLine !== null ? [selectedLine] : [];
            map.addSource('selectedLine', {
                type: 'geojson',
                data: { type: "FeatureCollection", features: features }
            });
        }

        if (!map.getSource("points")) {
            map.addSource('points', {
                type: 'geojson',
                data: { type: "FeatureCollection", features: [] }
            });
        }

        if (!map.getSource("selectedPoints")) {
            const selectedPointFeature = selectedPoint !== null ? [selectedPoint] : [];
            map.addSource('selectedPoints', {
                type: 'geojson',
                data: { type: "FeatureCollection", features: selectedPointFeature }

            });
        }

        if (!map.getSource("alarmedPoints")) {
            map.addSource("alarmedPoints", {
                type: 'geojson',
                data: { type: "FeatureCollection", features: [] }
            });
        }
    }

    private addCircleLayer = (map: mapboxgl.Map, id: string, source: string, circleColor: string, radius: number, strokeWidth: number, outerColor: string) => {

        map.addLayer({
            id: id,
            source: source,
            type: 'circle',
            paint: {
                'circle-color': circleColor,
                'circle-radius': radius,
                'circle-stroke-width': strokeWidth,
                'circle-stroke-color': outerColor
            }
        });
    }

    private addLineLayer = (map: mapboxgl.Map, id: string, source: string, color: string, width: number, filter: string[]) => {

        map.addLayer({
            'id': id,
            'type': 'line',
            'source': source,
            'layout': {
                'line-join': 'round',
                'line-cap': 'round'
            },
            'paint': {
                'line-color': color,
                'line-width': width
            },
            'filter': filter
        });
    }

    private addIconLayer = (map: mapboxgl.Map, id: string, source: string, iconName: string, iconSize: number, filter: (string | string[])[]) => {
        map.addLayer({
            'id': id,
            'type': 'symbol',
            'source': source,
            'layout': {
                'icon-allow-overlap': true,
                'icon-image': iconName,
                'icon-size': iconSize

            },
            'filter': filter,
        });
    }

    /**
     * Pick the correct theme based on user selection
     */
    private pickTheme = () => {
        if (this.selectedTheme !== null) {
            const result = this.settings.networkMapThemes.themes.find(el => el.key === this.selectedTheme);
            if (result)
                return result;

        }

        return this.settings.networkMapThemes.themes[0];

    }

    public addBaseLayers = (map: mapboxgl.Map, themesettings?: ThemeElement) => {

        const theme = !themesettings ? this.pickTheme() : themesettings;
        console.log("user selected theme: " + this.selectedTheme)
        console.log("found theme:" + theme);

        this.addCommonLayers(map);

        this.addCircleLayer(map, 'points', 'points', theme.site, 7, 1, '#fff');
        this.addCircleLayer(map, 'selectedPoints', 'selectedPoints', theme.selectedSite, 9, 1, '#fff');
        this.addCircleLayer(map, 'alarmedPoints', 'alarmedPoints', '#CC0000', 9, 1, '#fff');
    }

    public addIconLayers = (map: mapboxgl.Map, selectedSiteId?: string) => {

        this.addCommonLayers(map);
        this.createIconLayers(map, selectedSiteId);
    }

    private createIconLayers = (map: mapboxgl.Map, selectedSiteId?: string) => {

        this.addIconLayer(map, 'point-lamps', 'points', 'lamp', 0.1, this.createFilter("street lamp", selectedSiteId));
        this.addIconLayer(map, 'point-building', 'points', 'house', 0.1, this.createFilter("high rise building", selectedSiteId));
        this.addIconLayer(map, 'point-data-center', 'points', 'data-center', 0.1, this.createFilter("data center", selectedSiteId));
        this.addIconLayer(map, 'point-factory', 'points', 'factory', 0.2, this.createFilter("factory", selectedSiteId));


        //select layers
        this.addIconLayer(map, 'select-point-lamps', 'selectedPoints', 'lamp', 0.15, ['==', 'type', 'street lamp']);
        this.addIconLayer(map, 'select-point-buildings', 'selectedPoints', 'house', 0.15, ['==', 'type', 'high rise building']);
        this.addIconLayer(map, 'select-point-data-center', 'selectedPoints', 'data-center', 0.15, ['==', 'type', 'data center']);
        this.addIconLayer(map, 'select-point-factory', 'selectedPoints', 'factory', 0.3, ['==', 'type', 'factory']);

        //alarm layers
        this.addIconLayer(map, 'point-lamps-alarm', 'alarmedPoints', 'lamp-red', 0.3, this.createFilter("street lamp"));
        this.addIconLayer(map, 'point-building-alarm', 'alarmedPoints', 'house-red', 0.3, this.createFilter("high rise building"));
        this.addIconLayer(map, 'point-data-center-alarm', 'alarmedPoints', 'data-center-red', 0.3, this.createFilter("data center"));
        this.addIconLayer(map, 'point-factory-alarm', 'alarmedPoints', 'factory-red', 0.45, this.createFilter("factory"));

        map.addLayer({
            id: 'point-remaining',
            source: 'points',
            type: 'circle',
            'filter': ['none', ['==', 'type', "high rise building"], ['==', 'type', "data center"], ['==', 'type', "factory"], ['==', 'type', "street lamp"]],
            paint: {
                'circle-color': '#11b4da',
                'circle-radius': 7,
                'circle-stroke-width': 1,
                'circle-stroke-color': '#fff'
            }
        });
    }

    private addCommonLayers = (map: mapboxgl.Map, themesettings?: ThemeElement) => {

        const theme = !themesettings ? this.pickTheme() : themesettings;

        this.addLineLayer(map, 'microwave-lines', 'lines', theme.microwaveLink, 2, ['==', 'type', 'microwave']);
        this.addLineLayer(map, 'fibre-lines', 'lines', theme.fiberLink, 2, ['==', 'type', 'fibre']);
        this.addLineLayer(map, 'selectedLineMicrowave', 'selectedLine', theme.microwaveLink, 4, ['==', 'type', 'microwave']);
        this.addLineLayer(map, 'selectedLineFibre', 'selectedLine', theme.fiberLink, 4, ['==', 'type', 'fibre']);
    }

    public removeBaseLayers = (map: mapboxgl.Map) => {

        map.removeLayer("points");
        map.removeLayer("lines");
        map.removeLayer('selectedPoints');
        map.removeLayer('selectedLine');
    }

    private removeIconLayers = (map: mapboxgl.Map) => {

        map.removeLayer('point-building');
        map.removeLayer('point-lamps');
        map.removeLayer('point-data-center');
        map.removeLayer('point-factory');
        map.removeLayer('point-remaining');
        map.removeLayer('select-point-data-center');
        map.removeLayer('select-point-buildings');
        map.removeLayer('select-point-lamps');
        map.removeLayer('select-point-factory');
        map.removeLayer('point-building-alarm');
        map.removeLayer('point-lamps-alarm');
        map.removeLayer('point-data-center-alarm');
        map.removeLayer('point-factory-alarm');
    }


    private createFilter = (type: 'street lamp' | 'high rise building' | 'data center' | 'factory', selectedSiteId?: string) => {

        return selectedSiteId === undefined ? ['==', 'type', type] : ["all", ['==', 'type', type], ['!=', 'id', selectedSiteId]]
    }

    public showIconLayers = (map: mapboxgl.Map, show: boolean, selectedSiteId?: string) => {

        const zoom = map.getZoom();

        if (show) {

            if (zoom > 11) {

                const bounds = map.getBounds();

                if (map.getLayer('points') !== undefined && map.getLayer('point-lamps') === undefined && !this.checkedLayers) {

                    // if sites don't have a type don't change layers to icons
                    const elements = map.queryRenderedFeatures(undefined, {
                        layers: ['points'], filter: ['has', 'type']
                    });
                    this.checkedLayers = true;

                    if (elements.length > 0 && elements.length < 1000) {

                        if (map.getLayer('point-lamps') === undefined) {
                            map.removeLayer('points');
                            map.setLayoutProperty('alarmedPoints', 'visibility', 'none');
                            map.setLayoutProperty('selectedPoints', 'visibility', 'none');
                            this.createIconLayers(map, selectedSiteId);
                            //map.moveLayer('point-remaining','selectedPoints');

                        }
                    }
                }

            } else {
                this.swapLayersBack(map);
            }
        } else {
            this.swapLayersBack(map);
        }
    }

    public swapLayersBack = (map: mapboxgl.Map) => {
        this.checkedLayers = false;
        const theme = this.pickTheme();

        if (map.getLayer('selectedPoints') === undefined) {
            this.addCircleLayer(map, 'selectedPoints', 'selectedPoints', theme.selectedSite, 9, 1, '#fff');

        }

        if (map.getLayer('alarmedPoints') === undefined) {
            this.addCircleLayer(map, 'alarmedPoints', 'alarmedPoints', '#CC0000', 9, 1, '#fff');

        }


        if (map.getLayer('points') === undefined) {

            map.setLayoutProperty('selectedPoints', 'visibility', 'visible');
            map.setLayoutProperty('alarmedPoints', 'visibility', 'visible');
            this.removeIconLayers(map);

            this.addCircleLayer(map, 'points', 'points', theme.site, 7, 1, '#fff');


            map.moveLayer('points', map.getLayer('selectedPoints').id);
        }
    }

    public changeMapOpacity = (map: mapboxgl.Map, newValue: number) => {
        const newOpacity = newValue / 100;
        if (map) {
            const tiles = map.getStyle().layers?.filter(el => el.id.includes("tiles"))
            tiles?.forEach(layer => {
                if (layer.type === 'symbol') {
                    map.setPaintProperty(layer.id, `icon-opacity`, newOpacity);
                    map.setPaintProperty(layer.id, `text-opacity`, newOpacity);
                } else {
                    map.setPaintProperty(layer.id, `${layer.type}-opacity`, newOpacity);
                }
            })
        }

    }

    public changeTheme = (map: mapboxgl.Map, themeName: string) => {
        this.selectedTheme = themeName;
        const theme = this.pickTheme();
        if (theme && map.loaded()) {
            map.setPaintProperty('points', 'circle-color', theme.site);
            map.setPaintProperty('selectedPoints', 'circle-color', theme.selectedSite);
            map.setPaintProperty('microwave-lines', 'line-color', theme.microwaveLink);
            map.setPaintProperty('fibre-lines', 'line-color', theme.fiberLink);
        }
    }
}

const mapLayerService = new MapLayerService();
export default mapLayerService;


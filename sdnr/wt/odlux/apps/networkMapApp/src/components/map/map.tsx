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

import * as React from 'react'
import * as mapboxgl from 'mapbox-gl';
import { RouteComponentProps, withRouter } from 'react-router-dom';


import { Site } from '../../model/site';
import { SelectSiteAction, ClearHistoryAction, SelectLinkAction } from '../../actions/detailsAction';
import { OSM_STYLE, URL_API, URL_BASEPATH, URL_TILE_API } from '../../config';
import { link } from '../../model/link';
import MapPopup from './mapPopup';
import { SetPopupPositionAction, SelectMultipleLinksAction, SelectMultipleSitesAction } from '../../actions/popupActions';
import { Feature } from '../../model/Feature';
import { HighlightLinkAction, HighlightSiteAction, SetCoordinatesAction, SetStatistics } from '../../actions/mapActions';
import { addDistance, getUniqueFeatures, increaseBoundingBox } from '../../utils/mapUtils';
import { IApplicationStoreState } from '../../../../../framework/src/store/applicationStore';
import connect, { IDispatcher, Connect } from '../../../../../framework/src/flux/connect';
import SearchBar from './searchBar';
import { verifyResponse, IsTileServerReachableAction, handleConnectionError, setTileServerReachableAction, IsBusycheckingConnectivityAction } from '../../actions/connectivityAction';
import ConnectionInfo from './connectionInfo'
import  mapLayerService  from '../../utils/mapLayers';
import Statistics from './statistics';
import IconSwitch from './iconSwitch';
import { addImages } from '../../services/mapImagesService';
import { PopupElement } from '../../model/popupElements';
import { Button } from '@material-ui/core';
import { NavigateToApplication } from '../../../../../framework/src/actions/navigationActions';
import customize from '../../../icons/customize.png';

type coordinates = { lat: number, lon: number, zoom: number }

let alarmElements: Feature[] = [];
let map: mapboxgl.Map;
let isLoadingInProgress = false;
let notLoadedBoundingBoxes: mapboxgl.LngLatBounds[] = [];

let lastBoundingBox: mapboxgl.LngLatBounds | null = null;
let myRef = React.createRef<HTMLDivElement>();


class Map extends React.Component<mapProps, { isPopupOpen: boolean }> {

    constructor(props: mapProps) {
        super(props);
        //any state stuff
        this.state = { isPopupOpen: false }

    }

    updateTheme(){
        mapLayerService.settings=this.props.settings.themes;
        if(this.props.settings.mapSettings?.networkMap.styling.theme){
            mapLayerService.selectedTheme = this.props.settings.mapSettings?.networkMap.styling.theme;
        }
    }

    updateOpacity(){
        if(this.props.settings.mapSettings && this.props.settings.mapSettings.networkMap.tileOpacity){
            mapLayerService.changeMapOpacity(map, Number(this.props.settings.mapSettings.networkMap.tileOpacity));    
        }
    }

    async componentDidMount() {

        // resize the map, if menu gets collapsed
        window.addEventListener("menu-resized", this.handleResize);

        //pass themes to mapLayerService
        this.updateTheme();

        // try if connection to tile + topologyserver are available

        try {
            const tiles = await fetch(URL_TILE_API + '/10/0/0.png');
            if (tiles.ok) {
                this.props.setTileServerLoaded(true);
            }else{
                this.props.setTileServerLoaded(false);
            }

        } catch (error) {
            this.props.setTileServerLoaded(false);
            console.error("tileserver " + URL_TILE_API + " can't be reached.");
        }

        try {
           const topology = await fetch(URL_API + "/info/count/all");
            verifyResponse(topology);
        } catch (error) {
            this.props.handleConnectionError(error)
        }

        //both done
        this.props.setConnectivityCheck(false);
        //map loaded in componentDidUpdate
    }

    setupMap = () => {

        let lat = this.props.lat;
        let lon = this.props.lon;
        let zoom = this.props.zoom;

        if(this.props.settings.mapSettings){
            if(this.props.settings.mapSettings.networkMap.startupPosition.latitude){
                lat = Number(this.props.settings.mapSettings.networkMap.startupPosition.latitude)
            }

            if(this.props.settings.mapSettings.networkMap.startupPosition.longitude){
                lon = Number(this.props.settings.mapSettings.networkMap.startupPosition.longitude)
            }

            if(this.props.settings.mapSettings.networkMap.startupPosition.zoom){
                zoom = Number(this.props.settings.mapSettings.networkMap.startupPosition.zoom)
            }

        }

        const coordinates = this.extractCoordinatesFromUrl();
        // override lat/lon/zoom with coordinates from url, if available
        if (this.areCoordinatesValid(coordinates)) {
            lat = coordinates.lat;
            lon = coordinates.lon;
            zoom = !Number.isNaN(coordinates.zoom) ? coordinates.zoom : zoom;
        }

        map = new mapboxgl.Map({
            container: myRef.current!,
            style: OSM_STYLE as any,
            center: [lon, lat],
            zoom: zoom,
            accessToken: ''
        });

        map.on('load', (ev) => {

            map.setMaxZoom(18);
            const bbox = map.getBounds();
            this.props.updateMapPosition(bbox.getCenter().lat, bbox.getCenter().lng, map.getZoom())

            mapLayerService.addBaseSources(map, this.props.selectedSite, this.props.selectedLink);
            
            addImages(map, (result: boolean)=>{
                if(map.getZoom()>11)
                {
                    mapLayerService.addIconLayers(map, this.props.selectedSite?.properties.id)
                }else{
                    mapLayerService.addBaseLayers(map);
                }
            this.updateOpacity();

            });

            const boundingBox = increaseBoundingBox(map);

            fetch(`${URL_API}/links/geojson/${boundingBox.west},${boundingBox.south},${boundingBox.east},${boundingBox.north}`)
                .then(result => verifyResponse(result))
                .then(result => result.json())
                .then(features => {
                    if (map.getSource('lines')) {
                        (map.getSource('lines') as mapboxgl.GeoJSONSource).setData(features);
                    }
                })
                .catch(error => this.props.handleConnectionError(error));


            fetch(`${URL_API}/sites/geojson/${boundingBox.west},${boundingBox.south},${boundingBox.east},${boundingBox.north}`)
                .then(result => verifyResponse(result))
                .then(result => result.json())
                .then(features => {
                    if (map.getSource('points')) {
                        (map.getSource('points') as mapboxgl.GeoJSONSource).setData(features);
                    }
                })
                .catch(error => this.props.handleConnectionError(error));
        });

        map.on('click', (e: any) => {

            if (map.getLayer('points')) { // data is shown as points

                var clickedLines = getUniqueFeatures(map.queryRenderedFeatures([[e.point.x - 5, e.point.y - 5],
                [e.point.x + 5, e.point.y + 5]], {
                    layers: ['microwave-lines', 'fibre-lines']
                }), "id");

                const clickedPoints = getUniqueFeatures(map.queryRenderedFeatures(e.point, { layers: ['points'] }), "id");
                const alarmedSites = getUniqueFeatures(map.queryRenderedFeatures(e.point, { layers: ['alarmedPoints'] }), "id");

                if (clickedPoints.length != 0) {


                    if (alarmedSites.length > 0) {
                        alarmedSites.forEach(alarm => {
                            const index = clickedPoints.findIndex(item => item.properties!.id === alarm.properties!.id);

                            if (index !== -1) {
                                clickedPoints[index].properties!.alarmed = true;
                                clickedPoints[index].properties!.type = "alarmed";
                            }
                        });
                    }

                    this.showSitePopup(clickedPoints, e.point.x, e.point.y);
                } else if (clickedLines.length != 0) {
                    this.showLinkPopup(clickedLines, e.point.x, e.point.y);
                }


            } else { // data is shown as icons

                const clickedSites = getUniqueFeatures(map.queryRenderedFeatures(e.point, { layers: ['point-lamps', 'point-building', 'point-data-center', 'point-factory', 'point-remaining'] }), "id");
                const clickedLines = getUniqueFeatures(map.queryRenderedFeatures([[e.point.x - 5, e.point.y - 5],
                [e.point.x + 5, e.point.y + 5]], {
                    layers: ['microwave-lines', 'fibre-lines']
                }), "id");

                if (clickedSites.length > 0)
                    this.showSitePopup(clickedSites, e.point.x, e.point.y);
                else if (clickedLines.length != 0) {
                    this.showLinkPopup(clickedLines, e.point.x, e.point.y);
                }
            }

        });

        map.on('moveend', () => {

            const mapZoom = Number(map.getZoom().toFixed(2));
            const lat = Number(map.getCenter().lat.toFixed(4));
            const lon = Number(map.getCenter().lng.toFixed(4));


            if (this.props.lat !== lat || this.props.lon !== lon || this.props.zoom !== mapZoom) {
                this.props.updateMapPosition(lat, lon, mapZoom)
            }

            // update the url to current lat,lon,zoom values

            const currentUrl = window.location.href;
            const parts = currentUrl.split(URL_BASEPATH);
            if(parts.length>0){

                const detailsPath = parts[1].split("/details/");

                if (detailsPath[1] !== undefined && detailsPath[1].length > 0) {
                    this.props.history.replace(`/${URL_BASEPATH}/${map.getCenter().lat.toFixed(4)},${map.getCenter().lng.toFixed(4)},${mapZoom.toFixed(2)}/details/${detailsPath[1]}`)
                }
                else {
                    this.props.history.replace(`/${URL_BASEPATH}/${map.getCenter().lat.toFixed(4)},${map.getCenter().lng.toFixed(4)},${mapZoom.toFixed(2)}`)
                }
            }
           
            
            //switch icon layers if applicable
            mapLayerService.showIconLayers(map, this.props.showIcons, this.props.selectedSite?.properties.id);

            //update statistics
            const boundingBox = map.getBounds();

            fetch(`${URL_API}/info/count/${boundingBox.getWest()},${boundingBox.getSouth()},${boundingBox.getEast()},${boundingBox.getNorth()}`)
                .then(result => verifyResponse(result))
                .then(res => res.json())
                .then(result => {
                    if (result.links !== this.props.linkCount || result.sites !== this.props.siteCount) {
                        this.props.setStatistics(result.links, result.sites);
                    }
                })
                .catch(error => this.props.handleConnectionError(error));;
        })

        map.on('move', () => {
            const mapZoom = map.getZoom();

            const boundingBox = map.getBounds();

            this.loadNetworkData(boundingBox);
            if (mapZoom > 9) {

                if (map.getLayer('points')) {
                    map.setLayoutProperty('selectedPoints', 'visibility', 'visible');
                    map.setPaintProperty('points', 'circle-radius', 7);
                }
            } else {

                // reduce size of points / lines if zoomed out
                map.setPaintProperty('points', 'circle-radius', 2);
                map.setLayoutProperty('selectedPoints', 'visibility', 'none');

                if (mapZoom <= 4) {
                    map.setPaintProperty('fibre-lines', 'line-width', 1);
                    map.setPaintProperty('microwave-lines', 'line-width', 1);

                } else {
                    map.setPaintProperty('fibre-lines', 'line-width', 2);
                    map.setPaintProperty('microwave-lines', 'line-width', 2);
                }
            }
        });
    }

    componentDidUpdate(prevProps: mapProps, prevState: {}) {

        //(load map)
        //triggered if either settings were done loading or tile/topology server connectivity checked
        if(prevProps.settings !== this.props.settings || this.props.isConnectivityCheckBusy !== prevProps.isConnectivityCheckBusy){
            
            //update theme if settings changed
            if(prevProps.settings !== this.props.settings){
                this.updateTheme();
            }

            //if everything done loading/reachable, load map
            if(!this.props.isConnectivityCheckBusy && this.props.isTileServerReachable && !this.props.settings.isLoadingData && (prevProps.settings.isLoadingData !==this.props.settings.isLoadingData || prevProps.isConnectivityCheckBusy !== this.props.isConnectivityCheckBusy)){
                if(map == undefined){
                    this.setupMap();
                }
                else
                if(map.getContainer() !== myRef.current){ 
                    // reload map, because the current container (fresh div) doesn't hold the map and changing containers isn't supported
                    map.remove();
                    this.setupMap();
                }
            }
        }

        if (map !== undefined) {
            if (prevProps.selectedSite?.properties.id !== this.props.selectedSite?.properties.id) {

                if (this.props.selectedSite != null) {
                    if (map.getSource("selectedLine") !== undefined) {
                        (map.getSource("selectedLine") as mapboxgl.GeoJSONSource).setData({ type: "FeatureCollection", features: [] });
                        (map.getSource("selectedPoints") as mapboxgl.GeoJSONSource).setData({ type: "FeatureCollection", features: [this.props.selectedSite] });
                    }


                    if (map.getLayer('point-lamps') !== undefined) {

                        map.setFilter('point-lamps', ['==', 'type', 'street lamp']);
                        map.setFilter('point-data-center', ['==', 'type', 'data center']);
                        map.setFilter('point-building', ['==', 'type', 'high rise building']);
                        map.setFilter('point-factory', ['==', 'type', 'factory']);

                        if (this.props.selectedSite?.properties.type !== undefined) {
                            switch (this.props.selectedSite?.properties.type) {
                                case 'street lamp':
                                    map.setFilter('point-lamps', ["all", ['==', 'type', 'street lamp'], ['!=', 'id', this.props.selectedSite.properties.id]]);
                                    break;
                                case 'data center':
                                    map.setFilter('point-data-center', ["all", ['==', 'type', 'data center'], ['!=', 'id', this.props.selectedSite.properties.id]]);
                                    break;
                                case 'high rise building':
                                    map.setFilter('point-building', ["all", ['==', 'type', 'high rise building'], ['!=', 'id', this.props.selectedSite.properties.id]])
                                    break;
                                case 'factory':
                                    map.setFilter('point-factory', ["all", ['==', 'type', 'factory'], ['!=', 'id', this.props.selectedSite.properties.id]]);
                                    break;    
                            }
                        }
                    }


                }
                else 
                {
                    if (map.getSource("selectedPoints") !== undefined)
                        (map.getSource("selectedPoints") as mapboxgl.GeoJSONSource).setData({ type: "FeatureCollection", features: [] });

                }
            }

            if (prevProps.selectedLink !== this.props.selectedLink) {
                if (this.props.selectedLink != null) {

                    if (map.getLayer('point-lamps') !== undefined) {
                        map.setFilter('point-lamps', ['==', 'type', 'street lamp']);
                        map.setFilter('point-data-center', ['==', 'type', 'data center']);
                        map.setFilter('point-building', ['==', 'type', 'high rise building']);
                        map.setFilter('point-factory', ['==', 'type', 'factory']);
                    }

                    if (map.getSource("selectedLine") !== undefined) {
                        (map.getSource("selectedPoints") as mapboxgl.GeoJSONSource).setData({ type: "FeatureCollection", features: [] });
                        (map.getSource("selectedLine") as mapboxgl.GeoJSONSource).setData({ type: "FeatureCollection", features: [this.props.selectedLink] });
                    }
                }
                else 
                {
                    if (map.getSource("selectedLine") !== undefined)
                        (map.getSource("selectedLine") as mapboxgl.GeoJSONSource).setData({ type: "FeatureCollection", features: [] });
                }
            }

            if (prevProps.location.pathname !== this.props.location.pathname) {
                if (map) {
                    const coordinates = this.extractCoordinatesFromUrl();
                    this.moveMapToCoordinates(coordinates);
                }
            }

            if (prevProps.alarmlement !== this.props.alarmlement) {
                if (this.props.alarmlement !== null && !alarmElements.includes(this.props.alarmlement)) {
                    if (map.getSource("alarmedPoints"))
                        (map.getSource("alarmedPoints") as mapboxgl.GeoJSONSource).setData({ type: "FeatureCollection", features: alarmElements });
                    alarmElements.push(this.props.alarmlement)
                }
            }

            if (prevProps.showIcons !== this.props.showIcons) {
                if (map && map.getZoom() > 11) {
                    mapLayerService.showIconLayers(map, this.props.showIcons, this.props.selectedSite?.properties.id);
                }
            }

            if (prevProps.zoomToElement !== this.props.zoomToElement) {
                if (this.props.zoomToElement !== null) {
                    const currentZoom = map?.getZoom();

                    map.flyTo({
                        center: [
                            this.props.zoomToElement.lon,
                            this.props.zoomToElement.lat
                        ], zoom: currentZoom < 10 ? 10 : currentZoom,
                        essential: true
                    });
                }
            }
        }
    }

    componentWillUnmount(){
        window.removeEventListener("menu-resized", this.handleResize);
        lastBoundingBox=null;

        // will be checked again on next load
        this.props.setConnectivityCheck(true); 
    }

    handleResize = () => {
        if (map) {
            // wait a moment until resizing actually happened
            window.setTimeout(() => map.resize(), 500);
        }
    }

    extractCoordinatesFromUrl = (): coordinates => {
        const currentUrl = window.location.href;
        const mainPathParts = currentUrl.split(URL_BASEPATH);
        const coordinatePathPart = mainPathParts[1].split("/details/"); // split by details if present
        const allCoordinates = coordinatePathPart[0].replace("/", "");
        const coordinates = allCoordinates.split(",");
        return { lat: Number(coordinates[0]), lon: Number(coordinates[1]), zoom: Number(coordinates[2]) }
    }

    areCoordinatesValid = (coordinates: coordinates) => {

        if ((!Number.isNaN(coordinates.lat)) && (!Number.isNaN(coordinates.lon))) {
            return true;
        } else {
            return false;
        }
    }

    moveMapToCoordinates = (coordinates: coordinates) => {

        if (this.areCoordinatesValid(coordinates)) {
            let zoom = -1;

            if (!Number.isNaN(coordinates.zoom)) {
                zoom = coordinates.zoom;
            }

            map.flyTo({
                center: [
                    coordinates.lon,
                    coordinates.lat
                ], zoom: zoom !== -1 ? zoom : this.props.zoom,
                essential: true
            })
        }
    }

    loadNetworkData = async (bbox: mapboxgl.LngLatBounds) => {
        if (!isLoadingInProgress) { // only load data if loading not in progress
            isLoadingInProgress = true;

            if (lastBoundingBox == null) {
                lastBoundingBox = bbox;
                await this.draw('lines', `${URL_API}/links/geojson/${lastBoundingBox.getWest()},${lastBoundingBox.getSouth()},${lastBoundingBox.getEast()},${lastBoundingBox.getNorth()}`);
                await this.draw('points', `${URL_API}/sites/geojson/${lastBoundingBox.getWest()},${lastBoundingBox.getSouth()},${lastBoundingBox.getEast()},${lastBoundingBox.getNorth()}`);
            } else {

                // new bbox is bigger than old one
                if (bbox.contains(lastBoundingBox.getNorthEast()) && bbox.contains(lastBoundingBox.getSouthWest()) && lastBoundingBox !== bbox) {  //if new bb is bigger than old one

                    lastBoundingBox = bbox;

                    //calculate new boundingBox
                    const increasedBoundingBox = increaseBoundingBox(map);

                    await this.draw('lines', `${URL_API}/links/geojson/${increasedBoundingBox.west},${increasedBoundingBox.south},${increasedBoundingBox.east},${increasedBoundingBox.north}`);
                    await this.draw('points', `${URL_API}/sites/geojson/${increasedBoundingBox.west},${increasedBoundingBox.south},${increasedBoundingBox.east},${increasedBoundingBox.north}`);

                } else if (lastBoundingBox.contains(bbox.getNorthEast()) && lastBoundingBox.contains(bbox.getSouthWest())) { // last one contains new one
                    // bbox is contained in last one, do nothing
                    isLoadingInProgress = false;

                } else { // bbox is not fully contained in old one, extend

                    lastBoundingBox.extend(bbox);

                    await this.draw('lines', `${URL_API}/links/geojson/${lastBoundingBox.getWest()},${lastBoundingBox.getSouth()},${lastBoundingBox.getEast()},${lastBoundingBox.getNorth()}`);
                    await this.draw('points', `${URL_API}/sites/geojson/${lastBoundingBox.getWest()},${lastBoundingBox.getSouth()},${lastBoundingBox.getEast()},${lastBoundingBox.getNorth()}`);
                }

            }


            if (notLoadedBoundingBoxes.length > 0) { // load last not loaded boundingbox
                this.loadNetworkData(notLoadedBoundingBoxes.pop()!)
                notLoadedBoundingBoxes = [];
            }

        } else {
            notLoadedBoundingBoxes.push(bbox);
        }
    }

    showSitePopup = (sites: mapboxgl.MapboxGeoJSONFeature[], top: number, left: number) => {
        if (sites.length > 1) {
            const elements: PopupElement[] = sites.map(feature =>  {return {name: feature.properties!.name, id: feature.properties!.id}});

            this.props.setPopupPosition(top, left);
            this.props.selectMultipleSites(elements); //name, id object container
            this.setState({ isPopupOpen: true });

        } else {
            const id = sites[0].properties!.id;

            fetch(`${URL_API}/sites/${id}`)
                .then(result => verifyResponse(result))
                .then(res => res.json() as Promise<Site>)
                .then(result => {
                    this.props.selectSite(result);
                    this.props.highlightSite(result);
                    this.props.clearDetailsHistory();
                })
                .catch(error => this.props.handleConnectionError(error));;
        }
    }

    showLinkPopup = (links: mapboxgl.MapboxGeoJSONFeature[], top: number, left: number) => {

        if (links.length > 1) {

            const elements: PopupElement[] = links.map(feature =>  {return {name: feature.properties!.name, id: feature.properties!.id}});

            this.props.setPopupPosition(top, left);
            this.props.selectMultipleLinks(elements);
            this.setState({ isPopupOpen: true });

        } else {
            var id = links[0].properties!.id;

            fetch(`${URL_API}/links/${id}`)
                .then(result => verifyResponse(result))
                .then(res => res.json() as Promise<link>)
                .then(result => {
                    this.props.selectLink(result);
                    this.props.highlightLink(result);

                    this.props.clearDetailsHistory();
                })
                .catch(error => this.props.handleConnectionError(error));;
        }
    }

    draw = async (layer: string, url: string) => {

        fetch(url)
            .then(result => verifyResponse(result))
            .then(res => res.json())
            .then(result => {
                isLoadingInProgress = false;
                if (map.getSource(layer)) {
                    (map.getSource(layer) as mapboxgl.GeoJSONSource).setData(result);
                }
            })
            .catch(error => this.props.handleConnectionError(error));;
    }

    render() {

        return <>

{
    !this.props.settings.isLoadingData ?

            <div id="map" style={{ width: "70%", position: 'relative' }} ref={myRef} >
                {
                    this.state.isPopupOpen &&
                    <MapPopup onClose={() => { this.setState({ isPopupOpen: false }); }} />
                }
                <SearchBar />
                <Statistics />
                <IconSwitch visible={this.props.zoom>11} />
                <ConnectionInfo />
                <Button 
                disabled={!this.props.isTopoServerReachable}
                style={{'position': 'absolute', 'right':5, top:5, backgroundColor:'white'}}
                onClick={e => this.props.navigateToApplication("network", "customize")} >
                    <img src={customize} />
                </Button>
            </div>
            :<div style={{ width: "70%", position: 'relative' }} />

    }
        </>
    }

}

type mapProps = RouteComponentProps & Connect<typeof mapStateToProps, typeof mapDispatchToProps>;

const mapStateToProps = (state: IApplicationStoreState) => ({
    selectedLink: state.network.map.selectedLink,
    selectedSite: state.network.map.selectedSite,
    zoomToElement: state.network.map.zoomToElement,
    alarmlement: state.network.map.alarmlement,
    lat: state.network.map.lat,
    lon: state.network.map.lon,
    zoom: state.network.map.zoom,
    linkCount: state.network.map.statistics.links,
    siteCount: state.network.map.statistics.sites,
    isTopoServerReachable: state.network.connectivity.isToplogyServerAvailable,
    isTileServerReachable: state.network.connectivity.isTileServerAvailable,
    isConnectivityCheckBusy: state.network.connectivity.isBusy,
    showIcons: state.network.map.allowIconSwitch,
    settings: state.network.settings,
});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({
    selectSite: (site: Site) => dispatcher.dispatch(new SelectSiteAction(site)),
    selectLink: (link: link) => dispatcher.dispatch(new SelectLinkAction(link)),
    clearDetailsHistory: () => dispatcher.dispatch(new ClearHistoryAction()),
    selectMultipleLinks: (ids: PopupElement[]) => dispatcher.dispatch(new SelectMultipleLinksAction(ids)),
    selectMultipleSites: (ids: PopupElement[]) => dispatcher.dispatch(new SelectMultipleSitesAction(ids)),
    setPopupPosition: (x: number, y: number) => dispatcher.dispatch(new SetPopupPositionAction(x, y)),
    highlightLink: (link: link) => dispatcher.dispatch(new HighlightLinkAction(link)),
    highlightSite: (site: Site) => dispatcher.dispatch(new HighlightSiteAction(site)),
    updateMapPosition: (lat: number, lon: number, zoom: number) => dispatcher.dispatch(new SetCoordinatesAction(lat, lon, zoom)),
    setStatistics: (linkCount: string, siteCount: string) => dispatcher.dispatch(new SetStatistics(siteCount, linkCount)),
    setTileServerLoaded: (reachable: boolean) => dispatcher.dispatch(setTileServerReachableAction(reachable)),
    handleConnectionError: (error: Error) => dispatcher.dispatch(handleConnectionError(error)),
    navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path, "test3")),
    setConnectivityCheck: (done: boolean) => dispatcher.dispatch(new IsBusycheckingConnectivityAction(done)),

})

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Map));
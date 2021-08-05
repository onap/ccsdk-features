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

import * as React from 'react'
import * as mapboxgl from 'mapbox-gl';
import { render } from 'react-dom';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { OSM_STYLE, URL_BASEPATH } from '../config';
import { GPSProfileResult } from '../model/GPSProfileResult';
import MapContextMenu from './mapContextMenu';
import { getGPSProfile } from '../services/heightService';
import { max, min } from '../utils/math';
import { HeightChart } from './heightChart';
import { makeStyles } from '@material-ui/core';
import { ClearSavedChartAction, SetChartAction, SetEndpointAction, SetHeightA, SetHeightB, SetMapCenterAction, SetStartPointAction } from '../actions/mapActions';
import { LatLon } from '../model/LatLon';
import MapInfo from './mapInfo';
import { Height } from 'model/Height';
import { PictureAsPdf } from '@material-ui/icons';
import ConnectionErrorPoup from './ConnectionErrorPoup';
import { addBaseLayer, addBaseSource, addPoint } from '../utils/map';
import { SetReachableAction } from '../actions/commonActions';

import 'mapbox-gl/dist/mapbox-gl.css';

type mapProps = RouteComponentProps & Connect<typeof mapStateToProps, typeof mapDispatchToProps>;

const mapStateToProps = (state: IApplicationStoreState) => ({
    center: state.lineOfSight.map.center,
    zoom: state.lineOfSight.map.zoom,
    start: state.lineOfSight.map.start,
    end: state.lineOfSight.map.end,
    heightA: state.lineOfSight.map.heightA,
    heightB: state.lineOfSight.map.heightB,
    ready: state.lineOfSight.map.ready
});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({
  ClearChartAction: () => dispatcher.dispatch(new ClearSavedChartAction),
  SetMapPosition: (point: LatLon, zoom: number) => dispatcher.dispatch(new SetMapCenterAction(point, zoom)),
  SetHeightStart: (height: Height) => dispatcher.dispatch(new SetHeightA(height)),
  SetHeightEnd: (height: Height) => dispatcher.dispatch(new SetHeightB(height)),
  setStartPosition: (position: LatLon|null) => dispatcher.dispatch(new SetStartPointAction(position)),
  setEndPosition: (position: LatLon|null) => dispatcher.dispatch(new SetEndpointAction(position)),
  setReachable : (reachable: boolean |null) => dispatcher.dispatch(new SetReachableAction(reachable)),

  

})


let map: mapboxgl.Map;

const styles = makeStyles({
    chart: {
        position: "absolute",
        top: 0,
        bottom: 0,
        left: 0,
        right: 0
      
    }
  });


const Map: React.FC<mapProps> = (props) => {

  //const [start, setStart] = React.useState<mapboxgl.LngLat| undefined>();
  //const [end, setEnd] = React.useState<mapboxgl.LngLat| undefined>();
  const [data, setData] = React.useState<GPSProfileResult[] | number>(Number.NaN);
  const [dataMin, setDataMin] = React.useState<GPSProfileResult|undefined>();
  const [dataMax, setDataMax] = React.useState<GPSProfileResult|undefined>();
  const [isMapLoaded, setMapLoaded] = React.useState<boolean>(false);


const mapRef = React.useRef<{ map: mapboxgl.Map | null }>({ map: null });
const mapContainerRef = React.useRef<HTMLDivElement>(null);



const classes = styles();

const heightA = props.heightA !== null ? props.heightA.amsl +  props.heightA.antennaHeight : 0;
const heightB = props.heightB !== null ? props.heightB.amsl +  props.heightB.antennaHeight : 0;

const {start, end} = props;

const handleResize = () =>{

    if (map) {
        // wait a moment until resizing actually happened
        window.setTimeout(() => map.resize(), 500);
    }

}

//on mount
React.useEffect(()=>{

    window.addEventListener("menu-resized", handleResize);
    
   
    return () =>{
      console.log("unmount")
        window.removeEventListener("menu-resized", handleResize);

        const center = mapRef.current.map?.getCenter();
        const mapZoom = mapRef.current.map?.getZoom();
        if(center){
          props.SetMapPosition({latitude: center.lat, longitude:center.lng}, mapZoom!);
        }

        props.setReachable(null);
    }

},[]);


    React.useEffect(()=>{

        if(props.ready){
            setupMap();
        }

    },[props.ready]);

    React.useEffect(() => {
        if (props.ready && isMapLoaded) {
          drawChart();
          updateLosUrl();
        }

      }, [start, end, isMapLoaded]);

    const drawChart = () =>{
      if(start && end){

        addBaseSource(map, 'route');
        addBaseLayer(map, 'route');

        const json = `{
          "type": "Feature",
          "properties": {},
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [${start.longitude}, ${start.latitude}],
              [${end.longitude}, ${end.latitude}]
            ]}
          }`;
         
          
  
        (map.getSource("route") as mapboxgl.GeoJSONSource).setData(JSON.parse(json));
          
  
        getGPSProfile({ latitude: start.latitude, longitude: start.longitude }, { latitude: end.latitude, longitude: end.longitude }).then(data => {
          if (Array.isArray(data)) {
            setDataMin(min(data, d => d.height));
            setDataMax(max(data, d => d.height));
          }
          setData(data);
        });
      } 
      else if (start || end){

        const point = start!==null ? start: end!;
        addBaseSource(map, 'route');
        addBaseLayer(map, 'route');
        addPoint(map, point);

      }
      else {
        //delete layers and source
        //used instead of clearing source data because it has better performance 
        //(setting data to empty results in a noticable lag of line being cleared)
        mapRef.current.map?.getLayer('line') && mapRef.current.map?.removeLayer('line') && mapRef.current.map?.removeLayer('points') && mapRef.current.map?.removeSource('route');
      
      }
    }

    const updateLosUrl = () =>{

      if(start && end){
        
        const locationPart = `lat1=${start.latitude}&lon1=${start.longitude}&lat2=${end.latitude}&lon2=${end.longitude}`;

        let heightPart = '';

        if(props.heightA && props.heightB){
        heightPart = `&amslA=${props.heightA.amsl}&antennaHeightA=${props.heightA.antennaHeight}&amslB=${props.heightB.amsl}&antennaHeightB=${props.heightB.antennaHeight}`;
          
        }
          
        props.history.replace(`/${URL_BASEPATH}/los?${locationPart}${heightPart}`)
            
      }else if(!start && !end){
        props.history.replace(`/${URL_BASEPATH}`);
      }
    }

    
    const updateHeightA = (value:number, value2: number) =>{
      props.SetHeightStart({amsl: value, antennaHeight: value2});
    }

    const updateHeightB = (value:number, value2: number) =>{
      props.SetHeightEnd({amsl: value, antennaHeight: value2});
    }

    const OnEndPosition = (position: mapboxgl.LngLat) =>{
      props.setEndPosition({latitude: position.lat, longitude: position.lng})
    }

    const OnStartPosition = (position: mapboxgl.LngLat) =>{
      props.setStartPosition({latitude: position.lat, longitude: position.lng})
    }
    

    const setupMap = () => {

        let lat = props.center.latitude
        let lon = props.center.longitude;
        let zoom = props.zoom;

        map = new mapboxgl.Map({
            container: mapContainerRef.current!,
            style: OSM_STYLE as any,
            center: [lon, lat],
            zoom: zoom,
            accessToken: ''
        });

        mapRef.current.map = map;

        map.on('load', (ev) => {

            map.setMaxZoom(18);
            setMapLoaded(true);
           
            //add source, layer

            addBaseSource(map, 'route');
            addBaseLayer(map, 'route');

            });

            let currentPopup: mapboxgl.Popup | null = null;
            map.on('contextmenu', (e) => {

              if (currentPopup)
                currentPopup.remove();

                //change height if start/end changes
                //???  -> show value? / reset after chart display?
          
                const popupNode = document.createElement("div");
                render(
                  <MapContextMenu pos={e.lngLat}
                    onStart={(p) => { OnStartPosition(p); if (currentPopup) currentPopup.remove(); }}
                    onEnd={(p) => { OnEndPosition(p); if (currentPopup) currentPopup.remove(); }}
                    onHeightA={(p,p1)=> updateHeightA(p, p1)}
                    onHeightB={(p, p1)=> updateHeightB(p, p1)} />,
                  popupNode);
          
                currentPopup = new mapboxgl.Popup()
                  .setLngLat(e.lngLat)
                  .setDOMContent(popupNode)
                  .addTo(map);
              });

            map.on('moveend', mapMoveEnd);
               
        };

        const mapMoveEnd = () =>{
        const mapZoom = Number(map.getZoom().toFixed(2));
        const lat = Number(map.getCenter().lat.toFixed(4));
        const lon = Number(map.getCenter().lng.toFixed(4));

        props.SetMapPosition({latitude: lat, longitude: lon}, mapZoom);
        
        }
    


    return <>
    <div id="map" style={{ width: "100%", height:'100%', position: 'relative' }} ref={mapContainerRef} >
    <MapInfo minHeight={dataMin} maxHeight={dataMax}  />
    <ConnectionErrorPoup reachable={props.ready} />
    
    {typeof data === "object"
        ? (
          < div className={classes.chart} onClick={() => {
            setData(Number.NaN);
            setDataMax(undefined);
            setDataMin(undefined);
            props.ClearChartAction();
          }}>
            <HeightChart heightPosA={heightA} heightPosB={heightB} width={mapContainerRef.current?.clientWidth!} height={mapContainerRef.current?.clientHeight!} data={data} dataMin={dataMin!} dataMax={dataMax!} />
          </div>
        )
        : null
      }

        </div>
        </>
}

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Map));



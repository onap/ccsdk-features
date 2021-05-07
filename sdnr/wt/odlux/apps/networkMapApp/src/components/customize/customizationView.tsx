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

import { Button, Grid, InputLabel, makeStyles, MenuItem, Select, Slider, TextField, Typography } from '@material-ui/core';
import { NetworkMapSettings, ThemeElement } from '../../model/settings';
import * as React from 'react'
import connect, { Connect, IDispatcher } from '../../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../../framework/src/store/applicationStore';
import { updateSettings } from '../../actions/settingsAction';
import ThemeEntry from './themeElement'
import * as mapboxgl from 'mapbox-gl';
import { OSM_STYLE } from '../../config';
import mapLayerService from '../../utils/mapLayers';
import { requestRest } from '../../../../../framework/src/services/restService';
import { NavigateToApplication } from '../../../../../framework/src/actions/navigationActions';

type props = Connect<typeof mapProps, typeof mapDispatch>;
let map: mapboxgl.Map;
let myMapRef = React.createRef<HTMLDivElement>();
const default_boundingbox = "12.882544785787754,52.21421979821472,13.775455214211949,52.80406241672602";


const mapProps = (state: IApplicationStoreState) => ({
  settings: state.network.settings,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  updateSettings: (mapSettings: NetworkMapSettings) => dispatcher.dispatch(updateSettings(mapSettings)),
  navigateToApplication: (applicationName: string) => dispatcher.dispatch(new NavigateToApplication(applicationName)),


});

const styles = makeStyles({
  sectionMargin: {
    marginTop: "30px",
    marginBottom: "15px"
  },
  elementMargin: {

    marginLeft: "10px"
  }
});

const CustomizationView: React.FunctionComponent<props> = (props) => {

  const [opacity, setOpacity] = React.useState(Number(props.settings.mapSettings?.networkMap.tileOpacity) || 100);
  const [theme, setTheme] = React.useState(props.settings.mapSettings?.networkMap.styling.theme || '');
  const [latitude, setLatitude] = React.useState<number>(Number(props.settings.mapSettings?.networkMap.startupPosition.latitude)|| 52.5);
  const [longitude, setLongitude] = React.useState<number>(Number(props.settings.mapSettings?.networkMap.startupPosition.longitude)|| 13.35);
  const [zoom, setZoom] = React.useState<number>(Number(props.settings.mapSettings?.networkMap.startupPosition.zoom) || 10);


  //used to make opacity available within the map event-listeners
  //(hook state values are snapshotted at initalization and not updated afterwards, thus use a ref here)
  const myOpacityRef = React.useRef(opacity);
  const setOpacityState = (data:any) => {
    myOpacityRef.current = data;
    setOpacity(data);
  };

  const classes = styles();
  const currentTheme = props.settings.themes.networkMapThemes.themes.find(el => el.key === theme);


  React.useEffect(() => {
    mapLayerService.settings = props.settings.themes;

    map = new mapboxgl.Map({
      container: myMapRef.current!,
      style: OSM_STYLE as any,
      center: [longitude, latitude],
      zoom: zoom,
      accessToken: ''
    });

    map.on('load', (ev) => {

      mapLayerService.addBaseSources(map, null, null);
      if(props.settings.mapSettings?.networkMap.styling.theme !== theme){
        mapLayerService.addBaseLayers(map, currentTheme);

      }else{
        mapLayerService.addBaseLayers(map);
      }

      mapLayerService.changeMapOpacity(map, myOpacityRef.current);

      getData();
    });

    map.on('moveend', () => {
      const center = map.getCenter();
      setZoom(Number(map.getZoom().toFixed(4)));
      setLatitude(Number(center.lat.toFixed(4)));
      setLongitude(Number(center.lng.toFixed(4)));
    });

  }, []);

  React.useEffect(() => {
    recenterMap();
  }, [latitude, longitude, zoom]);

  const setState = () => {
    if (props.settings.mapSettings?.networkMap.styling) {
      setTheme(props.settings.mapSettings.networkMap.styling.theme);
      mapLayerService.changeTheme(map, props.settings.mapSettings.networkMap.styling.theme);
    }

    const propOpacity = props.settings.mapSettings?.networkMap.tileOpacity;
    if (propOpacity) {
      setOpacityState(propOpacity);
    }
  }

  React.useEffect(() => {
    setState();
  }, [props.settings.mapSettings]);

  const onOpacityChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>, newValue: number) => {
    setOpacity(newValue);
    mapLayerService.changeMapOpacity(map, newValue);

  };

  const onChangeTheme = (e: any) => {

    const newTheme = e.target.value;
    setTheme(newTheme);
    mapLayerService.changeTheme(map, newTheme);
  }

  const onCancel = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    e.preventDefault();
    props.navigateToApplication("network");
  }

  const onSaveSettings = async (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    e.preventDefault();

    const updatedSettings: NetworkMapSettings = {
      networkMap: {
        tileOpacity: opacity.toString(),
        styling: { theme: theme },
        startupPosition: {
          latitude: latitude.toString(),
          longitude: longitude.toString(),
          zoom: zoom.toString()
        }
      }
    };

    console.log(updatedSettings);
    
    await props.updateSettings(updatedSettings)
    props.navigateToApplication("network");

  }

  const recenterMap = () => {

    if (!isNaN(latitude) && !isNaN(longitude) && !isNaN(zoom))

      map.flyTo({
        center: [
          longitude,
          latitude
        ], zoom: zoom,
        essential: false
      });
  }



  const getData = () => {

    //get data of boundingbox from networkmap

    const links = requestRest<any>("/topology/network/links/geojson/" + default_boundingbox);
    const sites = requestRest<any>("/topology/network/sites/geojson/" + default_boundingbox);

    Promise.all([links, sites]).then(results => {
      if (map.getSource('lines')) {
        (map.getSource('lines') as mapboxgl.GeoJSONSource).setData(results[0]);
      }

      if (map.getSource('points')) {
        (map.getSource('points') as mapboxgl.GeoJSONSource).setData(results[1]);
      }

      if (map.getSource('selectedPoints')) {
        (map.getSource('selectedPoints') as mapboxgl.GeoJSONSource).setData(results[1].features[0]);
      }
    });
  }

  /**
   * Style property names to readable text
   * @param text propretyName
   * @returns readable text
   */
  const styleText = (text: string) => {
    const textParts = text.split(/(?=[A-Z])/); //split on uppercase character
    const newText = textParts.join(" ");
    return newText.charAt(0).toUpperCase() + newText.slice(1);
  }


  return (<>
    <h3>Settings</h3>
    <div style={{ display: 'flex', flexDirection: 'row', flexGrow: 1, height: "100%", position: 'relative' }}>
      <div style={{ width: "60%", flexDirection: 'column', position:'relative' }}>
        <Typography variant="body1" style={{ fontWeight: "bold" }} gutterBottom>Startup Position</Typography>
        <div style={{ display: 'flex', flexDirection: 'row' }}>
          <TextField type="number" value={latitude} onChange={(e) => setLatitude(e.target.value as any)} style={{ marginLeft: 10 }} label="Latitude" />
          <TextField type="number" value={longitude} onChange={(e) => setLongitude(e.target.value as any)} style={{ marginLeft: 5 }} label="Longitude" />
          <TextField type="number" value={zoom} onChange={(e) => setZoom(e.target.value as any)} style={{ marginLeft: 5 }} label="Zoom" />
        </div>

        <Typography className={classes.sectionMargin} variant="body1" style={{ fontWeight: "bold" }} gutterBottom>
          Tile Opacity
        </Typography>
        <Grid className={classes.elementMargin} container spacing={2} style={{ width: '50%' }}>
          <Grid item>0</Grid>
          <Grid item xs>
            <Slider color="secondary" min={0} max={100} value={opacity} onChange={onOpacityChange} aria-labelledby="continuous-slider" />
          </Grid>
          <Grid item>100</Grid>
        </Grid>

        <Typography className={classes.sectionMargin} variant="body1" style={{ fontWeight: "bold" }} gutterBottom>
          Style of properties
      </Typography>
        <InputLabel id="theme-select-label">Theme</InputLabel>
        <Select
          className={classes.elementMargin}
          value={theme}
          onChange={onChangeTheme}
          labelId="theme-select-label"
          style={{ marginLeft: 10 }}>
          {
            props.settings.themes.networkMapThemes.themes.map(el => <MenuItem value={el.key}>{el.key}</MenuItem>)
          }

        </Select>

        {
          currentTheme && <div style={{ marginLeft: 60 }}>
            { //skip the 'key' (theme name) entry
              Object.keys(currentTheme).slice(1).map(el => <ThemeEntry text={styleText(el)} color={(currentTheme as any)[el]} />)
            }
          </div>
        }


        <div className={classes.sectionMargin} style={{ position: 'absolute', right: 0, top: '60%' }}>
          <Button className={classes.elementMargin} variant="contained"
            color="primary" onClick={onCancel}>Cancel</Button>

          <Button className={classes.elementMargin} variant="contained"
            color="secondary" onClick={onSaveSettings}>Save</Button>
        </div>
      </div>
      <div id="map" ref={myMapRef} style={{ width: "35%", height: "50%" }}>

      </div>
    </div>

  </>)

}

export default connect(mapProps, mapDispatch)(CustomizationView);



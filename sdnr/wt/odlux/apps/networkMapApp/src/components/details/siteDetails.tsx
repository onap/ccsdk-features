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

import * as React from 'react';
import { TextField, Tabs, Tab, Typography, AppBar, Button, Tooltip } from '@material-ui/core';


import MaterialTable, { ColumnModel, ColumnType, MaterialTableCtorType } from "../../../../../framework/src/components/material-table";


import { site, Device } from '../../model/site';
import DenseTable from '../denseTable';
import { LatLonToDMS } from '../../utils/mapUtils';

type minLinks = { name: string, azimuth: string}

const FaultAlarmNotificationTable = MaterialTable as MaterialTableCtorType<minLinks>;


type panelId="links" | "nodes";
type props = { site: site, updatedDevices: Device[]|null, navigate(applicationName: string, path?: string):void, onLinkClick(id: string): void, loadDevices(devices:Device[]): void };

const SiteDetails: React.FunctionComponent<props> = (props) => {

    const [value, setValue] = React.useState<panelId>("links");
    const [height, setHeight] = React.useState(330);

    const handleResize = () =>{
        const el = document.getElementById('site-details-panel')?.getBoundingClientRect();
        const el2 = document.getElementById('site-tabs')?.getBoundingClientRect();

        if(el && el2){
            setHeight(el!.height - el2!.y +20);
        }
        
    }

    //on mount
    React.useEffect(()=>{
        handleResize();

        window.addEventListener("resize", ()=>{console.log("really got resized.")});
    },[]);

    // on update
    React.useEffect(()=>{

        props.loadDevices(props.site.devices);
        handleResize();

    }, [props.site])

    const onHandleTabChange = (event: React.ChangeEvent<{}>, newValue: panelId) => {
        setValue(newValue);
    }

    const linkRows: minLinks[] = props.site.links.map(link=> 
        { 
            return {name: link.name, azimuth: link.azimuthB.toFixed(2) }   
        });



    return (<div  style={{ padding: '15px', display: "flex", flexDirection:"column", minWidth:0, minHeight:0 }}>
        <h2 >{props.site.name}</h2>
        {
            props.site.operator !== '' && props.site.operator !== null ?
                <TextField inputProps={{ 'aria-label': 'operator' }} disabled={true} value={props.site.operator} label="Operator" /> :
                <TextField inputProps={{ 'aria-label': 'operator' }} disabled={true} value="Unkown" label="Operator" style={{ marginTop: "5px" }} />
        }
        {
            props.site.type !== undefined && props.site.type.length > 0 &&
            <TextField inputProps={{ 'aria-label': 'type' }} disabled={true} value={props.site.type} label="Type" style={{ marginTop: "5px" }} />
        }
        {
            props.site.address !== undefined && props.site.address.length > 0 &&
            <TextField inputProps={{ 'aria-label': 'adress' }} disabled={true} value={props.site.address} label="Adress" style={{ marginTop: "5px" }} />
        }
        {
            props.site.heighAGLInMeters !== undefined && props.site.heighAGLInMeters > 0 &&
            <TextField inputProps={{ 'aria-label': 'amsl-in-meters' }} disabled={true} value={props.site.heighAGLInMeters} label="AMSL in meters" style={{ marginTop: "5px" }} />
        }
        {
            props.site.antennaHeightAGLInMeters !== undefined && props.site.antennaHeightAGLInMeters > 0 &&
            <TextField inputProps={{ 'aria-label': 'antenna-above-ground-in-meters' }} disabled={true} value={props.site.antennaHeightAGLInMeters} label="Atenna above ground in meters" style={{ marginTop: "5px" }} />
        }
         
        <TextField inputProps={{ 'aria-label': 'latitude' }} style={{ marginTop: "5px" }} disabled={true} value={LatLonToDMS(props.site.geoLocation.lat)} label="Latitude" />
        <TextField inputProps={{ 'aria-label': 'longitude' }} style={{ marginTop: "5px" }} disabled={true} value={LatLonToDMS(props.site.geoLocation.lon, true)} label="Longitude" />

        <AppBar position="static" style={{ marginTop: "5px", background: '#2E3B55' }}>
            <Tabs id="site-tabs" value={value} onChange={onHandleTabChange} aria-label="simple tabs example">
                <Tab label="Links" value="links" />
                <Tab label="Nodes" value="nodes" />
            </Tabs>
        </AppBar>
        {
            value === "links" &&
            <>
                {
                    props.site.links.length === 0 &&
                    <Typography aria-label="no-links-available" variant="body1" style={{ marginTop: '10px' }}>No links available.</Typography>
                }
               
                {
                    props.site.links.length > 0 &&
                    <DenseTable ariaLabelRow="available-links-table" ariaLabelColumn={["link-name", "azimuth"]} height={height} hover={true} headers={["Link Name", "Azimuth in °"]}  data={linkRows} onClick={props.onLinkClick}  ></DenseTable>
               /**
                * 
                * */
                
               
               }

            </>

        }
        {
            value === "nodes" &&
            <>
                {
                    props.site.devices.length === 0 &&
                    <Typography aria-label="no-nodes-avilable" variant="body1" style={{ marginTop: '10px' }}>No nodes available.</Typography>
                }

                {
                    props.site.devices.length>0 && props.updatedDevices !== null &&
                    <DenseTable ariaLabelRow="available-nodes-table" ariaLabelColumn={["id","name","type", "manufacturer","owner","status", "ports", "actions"]} navigate={props.navigate} height={height} hover={false} headers={["ID","Name","Type", "Manufacturer","Owner","Status", "Ports", "Actions"]} actions={true} data={props.updatedDevices!} />
                }
            </>
        }
    </div>
    )

}

export default SiteDetails;
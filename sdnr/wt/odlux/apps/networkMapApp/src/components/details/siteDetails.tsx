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


import { Site, Device, Address } from '../../model/site';
import DenseTable from '../denseTable';
import { LatLonToDMS } from '../../utils/mapUtils';
import { CheckDeviceList, InitializeLoadedDevicesAction } from '../../actions/detailsAction';
import { NavigateToApplication } from '../../../../../framework/src/actions/navigationActions';
import connect, { Connect, IDispatcher } from '../../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../../framework/src/store/applicationStore';

type linkRow = { name: string, azimuth?: string}
type deviceRow = { id: string;type: string,name: string,manufacturer: string,owner: string,status?: string,port: number[]}


type panelId="links" | "nodes";
type siteDetailProps = {
    site: Site, 
    onLinkClick(id: string): void, 
} & props;

type props =  Connect<typeof mapStateToProps, typeof mapDispatchToProps>;


const SiteDetails: React.FunctionComponent<siteDetailProps> = (props) => {

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
        
        if(props.site.devices!== null && props.site.devices.length>0){
            props.initializeDevices(props.site.devices);
            props.loadDevices(props.site.devices);
        }
      
        handleResize();

    }, [props.site]);

    const onHandleTabChange = (event: React.ChangeEvent<{}>, newValue: panelId) => {
        setValue(newValue);
    }

    //prepare link table

    let hasAzimuth = false;
    const linkRows: linkRow[] = props.site.links.map(link=> 
        { 
            if(link.azimuthB!==null){
                hasAzimuth=true;
                return {name: link.name, azimuth: link.azimuthB.toFixed(2) }   

            }else{
                return {name: link.name }   
            }
            
        });
       
    const linkTableHeader = hasAzimuth ?  ["Link Name", "Azimuth in °"] : ["Link Name"];

    //prepare device table
    const deviceRows : deviceRow[] = props.updatedDevices.map(device=>{
        return{ 
            id: device.id,
            name: device.name,
            type: device.type,
            status: device.status,
            manufacturer: device.manufacturer,
            owner: device.owner,
            port: device.port
        }
    });

    const adressString = props.site.address == null ? null : buildAdress(props.site.address);


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
            adressString !== null && 
            <TextField inputProps={{ 'aria-label': 'adress' }} disabled={true} value={adressString} label="Address" style={{ marginTop: "5px" }} />
        }
        {
            props.site.heightAmslInMeters !== undefined && props.site.heightAmslInMeters > 0 &&
            <TextField inputProps={{ 'aria-label': 'amsl-in-meters' }} disabled={true} value={props.site.heightAmslInMeters} label="AMSL in meters" style={{ marginTop: "5px" }} />
        }
        {
            props.site.antennaHeightAmslInMeters !== undefined && props.site.antennaHeightAmslInMeters > 0 &&
            <TextField inputProps={{ 'aria-label': 'antenna-above-ground-in-meters' }} disabled={true} value={props.site.antennaHeightAmslInMeters} label="Atenna above ground in meters" style={{ marginTop: "5px" }} />
        }
         
        <TextField inputProps={{ 'aria-label': 'latitude' }} style={{ marginTop: "5px" }} disabled={true} value={LatLonToDMS(props.site.location.lat)} label="Latitude" />
        <TextField inputProps={{ 'aria-label': 'longitude' }} style={{ marginTop: "5px" }} disabled={true} value={LatLonToDMS(props.site.location.lon, true)} label="Longitude" />

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
                    <DenseTable ariaLabelRow="available-links-table" ariaLabelColumn={["link-name", "azimuth"]} height={height} hover={true} headers={linkTableHeader}  data={linkRows} onClick={props.onLinkClick}  ></DenseTable>
                }

            </>

        }
        {
            value === "nodes" &&
            <>
                {
                    props.site.devices === null &&
                    <Typography aria-label="no-nodes-avilable" variant="body1" style={{ marginTop: '10px' }}>No nodes available.</Typography>
                }

                {
                    props.site.devices?.length>0 && props.updatedDevices !== null &&
                    <DenseTable ariaLabelRow="available-nodes-table" ariaLabelColumn={["id","name","type","status", "manufacturer","owner", "ports", "actions"]} navigate={props.navigateToApplication} height={height} hover={false} headers={["ID","Name","Type","Status", "Manufacturer","Owner", "Ports", "Actions"]} actions={true} data={deviceRows!} />
                }
            </>
        }
    </div>
    )
}

const buildAdress = (adress: Address) =>{

    switch(adress.country){
        case "de":
            return `${adress.streetAndNr}, ${adress.zipCode!== null? adress.zipCode : ''} ${adress.city}`
        
        case "us": 
        return `${adress.streetAndNr}, ${adress.city} ${adress.zipCode!== null? adress.zipCode : ''}`

        default:
            console.log("address formatting for country {"+adress.country+"} not recognized, defaulting.");
            return `${adress.streetAndNr}, ${adress.zipCode!== null? adress.zipCode : ''} ${adress.city}`
    }

    
}

const mapStateToProps = (state: IApplicationStoreState) => ({
    updatedDevices: state.network.details.checkedDevices
});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({
    initializeDevices: (devices: Device[]) => {dispatcher.dispatch(new InitializeLoadedDevicesAction(devices))},
    loadDevices: async (networkElements: Device[]) => { await dispatcher.dispatch(CheckDeviceList(networkElements)) },
    navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path, "test3")),

})

export default connect(mapStateToProps, mapDispatchToProps)(SiteDetails);
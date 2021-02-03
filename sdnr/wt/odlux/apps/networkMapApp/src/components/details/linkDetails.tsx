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

import { link } from '../../model/link';
import { TextField, Tabs, Tab, Typography, AppBar, Button, Link } from '@material-ui/core';
import DenseTable from '../denseTable';
import { LatLonToDMS } from '../../utils/mapUtils';

type panelId = "siteA" | "siteB";
type props = { link: link };

const LinkDetails: React.FunctionComponent<props> = (props) => {

    const [value, setValue] = React.useState<panelId>("siteA");
    const [height, setHeight] = React.useState(330);

    const handleResize = () =>{
        const el = document.getElementById('link-details-panel')?.getBoundingClientRect();
        const el2 = document.getElementById('site-tabs')?.getBoundingClientRect();

        if(el && el2){
            if(props.link.type==="microwave")
              setHeight(el!.height - el2!.y -30);
            else
              setHeight(el!.height - el2!.y +20);

        }
    }

    //on mount
    React.useEffect(()=>{
        handleResize();

        //window.addEventListener("resize", handleResize);
    },[]);

    React.useEffect(()=>{
        handleResize();
    }, [props.link])

    const onHandleTabChange = (event: React.ChangeEvent<{}>, newValue: panelId) => {
        setValue(newValue);
    }

    const onCalculateLinkClick = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) =>{
       e.preventDefault();
       const siteA= props.link.locationA;
       const siteB =props.link.locationB;
       const nameA = props.link.siteA;
       const nameB = props.link.siteB;
       const distance = props.link.length > 0 ? props.link.length : props.link.calculatedLength;
       const azimuthA = props.link.azimuthA;
       const azimuthB = props.link.azimuthB;
       window.open(`/#/linkCalculation?lat1=${siteA.lat}&lon1=${siteA.lon}&lat2=${siteB.lat}&lon2=${siteB.lon}&siteA=${nameA}&siteB=${nameB}&azimuthA=${azimuthA}&azimuthB=${azimuthB}&distance=${distance}&amslSiteA=${siteA.amsl}&AGLsiteA=${siteA.antennaHeight}&amslSiteB=${siteB.amsl}&AGLsiteB=${siteB.antennaHeight}`)

    }

    const data = [

   {name:"Site Name", val1: props.link.siteA, val2: props.link.siteB},
    {name:"Latitude", val1: LatLonToDMS(props.link.locationA.lat), val2: LatLonToDMS(props.link.locationB.lat)},
    {name:"Longitude", val1: LatLonToDMS(props.link.locationA.lon, true), val2: LatLonToDMS(props.link.locationB.lon, true)},
    {name:"Azimuth in °", val1: props.link.azimuthA.toFixed(2), val2: props.link.azimuthB.toFixed(2)}
];

    return (<div style={{ paddingLeft: "15px", paddingRight: "15px", paddingTop: "0px", display: 'flex', flexDirection: 'column' }}>
        <h2>{props.link.name}</h2>
        <TextField inputProps={{ 'aria-label': 'operator' }} disabled style={{ marginTop: "5px" }} value="Unkown" label="Operator" />
        <TextField inputProps={{ 'aria-label': 'type' }} disabled style={{ marginTop: "5px" }} value={props.link.type} label="Type" />
        <TextField inputProps={{ 'aria-label': 'planned-distance-in-km' }} disabled style={{ marginTop: "5px" }} value={props.link.length.toFixed(2)} label="Distance planned in km" />
        <TextField inputProps={{ 'aria-label': 'calculated-distance-in-km' }} disabled style={{ marginTop: "5px" }} value={props.link.calculatedLength.toFixed(2)} label="Distance calculated in km" />

        <AppBar position="static" id="site-tabs" style={{ marginTop: "20px", background: '#2E3B55' }}>
            <Typography aria-label="details-of-link-sites" style={{ margin:"5px"}}>SITE DETAILS</Typography>
        </AppBar>
        <DenseTable ariaLabelRow="site-information-table-entry" ariaLabelColumn={["site-name", "latitude", "longitude", "azimuth"]} verticalTable height={height} hover={false} headers={["", "Site A", "Site B"]} data={data} />
        {
            props.link.type==="microwave" && <Button style={{marginTop:20}} fullWidth variant="contained" color="primary" onClick={onCalculateLinkClick}>Calculate link</Button>
        }
    </div>)
}

export default LinkDetails;
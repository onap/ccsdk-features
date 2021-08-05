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

import { Accordion, AccordionDetails, AccordionSummary, makeStyles, Paper, Typography } from '@material-ui/core';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import { GPSProfileResult } from '../model/GPSProfileResult';
import * as React from 'react';
import { calculateDistanceInMeter } from '../utils/map';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

const mapStateToProps = (state: IApplicationStoreState) => ({
    center: state.lineOfSight.map.center,
    zoom: state.lineOfSight.map.zoom,
    start: state.lineOfSight.map.start,
    end: state.lineOfSight.map.end,
    heightA: state.lineOfSight.map.heightA,
    heightB: state.lineOfSight.map.heightB,
});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({
  

})

type props =  Connect<typeof mapStateToProps, typeof mapDispatchToProps> & {
    minHeight: GPSProfileResult | undefined;
    maxHeight: GPSProfileResult | undefined;
};

const styles = (props: any) => makeStyles({
    accordion: {padding: 5, position: 'absolute', top: 10, width: props.width, marginLeft: 10, zIndex:1},
    container: { display: 'flex', flexDirection: "column", marginLeft:10, padding: 5 },
    caption:{width:'40%'},
    subTitleRow:{ width: '60%'},
    titleRowElement:{width: '40%', fontWeight: "bold"},
    secondRow:{width:'25%'},
    thirdRow:{width:'20%'}
  });

const MapInfo: React.FC<props> = (props) =>{

    const [expanded, setExpanded] = React.useState(false);
    const [width, setWidth] = React.useState(470);
    const [length, setLength] = React.useState<string | undefined>();

    const classes = styles({width: width})(); 

    const {start, end, center, zoom, heightA, heightB, minHeight, maxHeight} = props;

    React.useEffect(()=>{

        if(start && end){
            setLength(calculateDistanceInMeter(start.latitude, start.longitude, end.latitude, end.longitude).toFixed(3))

        }else{
            setLength(undefined)
        }

    }, [start, end])

    const handleChange = (event: any, isExpanded: boolean) => {
        setExpanded(isExpanded);
      };

   


    return <Accordion className={classes.accordion} expanded={expanded} onChange={handleChange}>
    <AccordionSummary
      expandIcon={<ExpandMoreIcon />}
      aria-controls="panel1a-content"
      id="panel1a-header"
    >
      <Typography >Map Info</Typography>
    </AccordionSummary>
    <AccordionDetails className={classes.container}>

    
    <Typography style={{ fontWeight: "bold", flex: "1" }} >Map Center</Typography>
    
    <div >
        <div style={{ display: 'flex', flexDirection: "row" }}> 
        <Typography className={classes.caption}> Longitude</Typography><Typography>{center.longitude}</Typography></div>
        <div style={{ display: 'flex', flexDirection: "row" }}> 
        <Typography className={classes.caption}> Latitude</Typography><Typography>{center.latitude}</Typography></div>
        <div style={{ display: 'flex', flexDirection: "row" }}> 
        <Typography className={classes.caption}> Zoom</Typography><Typography> {zoom}</Typography></div>
        
    </div>
    <Typography style={{ fontWeight: "bold", flex: "1", marginTop:5 }} >Link</Typography>
    
    <div>
    <div style={{ display: 'flex', flexDirection: "row", marginLeft:"38%" }}> 
    <Typography className={classes.titleRowElement}> Start</Typography>
    <Typography className={classes.titleRowElement}> End</Typography>
    </div>

    <div style={{ display: 'flex', flexDirection: "row" }}> 
    
    <Typography className={classes.caption}> Longitude</Typography>
    <Typography className={classes.secondRow}> {start?.longitude.toFixed(3)}</Typography>
    <Typography className={classes.secondRow}> {end?.longitude.toFixed(3)}</Typography></div>

    
    <div style={{ display: 'flex', flexDirection: "row" }}> 
    
    <Typography className={classes.caption}> Latitude</Typography>
    <Typography className={classes.secondRow}> {start?.latitude.toFixed(3)}</Typography>
    <Typography className={classes.secondRow}> {end?.latitude.toFixed(3)}</Typography></div>

    

    <div style={{ display: 'flex', flexDirection: "row" }}> 
    
    <Typography className={classes.caption}> Meassured height [m]</Typography>
    <Typography className={classes.secondRow}> {heightA?.amsl}</Typography>
    <Typography className={classes.secondRow}> {heightB?.amsl}</Typography>
    </div>

    <div style={{ display: 'flex', flexDirection: "row" }}> 
    
    <Typography className={classes.caption}> Antenna height [m] </Typography>
    <Typography className={classes.secondRow}> {heightA?.antennaHeight}</Typography>
    <Typography className={classes.secondRow}> {heightB?.antennaHeight}</Typography>
    </div>

    <div style={{ display: 'flex', flexDirection: "row" }}> 
    
    <Typography className={classes.caption}> Length [m]</Typography>
    <Typography className={classes.secondRow}> {length}</Typography>
  
    </div>

    <div style={{ display: 'flex', flexDirection: "row" }}> 
    
    <Typography className={classes.caption}> Max height @ position </Typography>
    <Typography className={classes.thirdRow}> {maxHeight? maxHeight.height+' m': ''}</Typography>
    <Typography className={classes.thirdRow}> {maxHeight?.gps.longitude.toFixed(3)}</Typography>
    <Typography className={classes.thirdRow}> {maxHeight?.gps.latitude.toFixed(3)}</Typography>
    </div>

    <div style={{ display: 'flex', flexDirection: "row" }}> 
    
    <Typography className={classes.caption}> Min height @ position</Typography>
    <Typography className={classes.thirdRow}> {minHeight? minHeight.height +' m': ''}</Typography>
    <Typography className={classes.thirdRow}> {minHeight?.gps.longitude.toFixed(3)}</Typography>
    <Typography className={classes.thirdRow}> {minHeight?.gps.latitude.toFixed(3)}</Typography>
    </div>

    </div>
</AccordionDetails>
</Accordion>
}

export default connect(mapStateToProps, mapDispatchToProps)(MapInfo);
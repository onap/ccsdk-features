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

import { Button, InputAdornment, makeStyles, TextField, Tooltip } from "@material-ui/core";
import * as React from "react";
import { FC, useEffect, useState } from "react";
import { getGPSHeight } from "../services/heightService";

type MapContextMenuProps = {
    pos: mapboxgl.LngLat;
    onStart: (pos: mapboxgl.LngLat) => void;
    onEnd: (pos: mapboxgl.LngLat) => void;
    onHeightA: (height: number, antennaHeight: number) => void;
    onHeightB: (height: number, antennaHeight: number) => void;

  }

  const styles = makeStyles({
    flexContainer: {display: "flex", flexDirection:"row"},
    textField:{width:60},
    button:{marginRight:5, marginTop:5, flexGrow:2}
  });
  
  const MapContextMenu: FC<MapContextMenuProps> = (props) => {
    const { pos, onStart, onEnd } = props;
    const [height, setHeight] = useState<number | undefined>(undefined);
    const [value1, setValue1] = useState<string>('');
    const [value2, setValue2] = useState<string>('');

    const classes = styles();
  
    useEffect(() => {
      getGPSHeight({ longitude: pos.lng, latitude: pos.lat }).then(setHeight);
    }, [pos.lat, pos.lng]);

    const handleChangeHeight = (e:React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>, id: "heightA"|"heightB") =>{
      
      //sanitize non numbers
      const onlyNums = e.target.value.replace(/[^0-9]/g, '');
      
      if(id==="heightA"){
        setValue1(onlyNums);
      }else{
        setValue2(onlyNums);
      }
    }
  
    return (
      <div>
        <div>Height: {height} m</div>
        <div>
        <div className={classes.flexContainer}>
          <Button className={classes.button} variant="contained" onClick={() => { onStart(pos); props.onHeightA(height!,+value1); }}>Start</Button>
          <Tooltip title="Please add the antenna height in meters above sea level.">
          <TextField className={classes.textField} value={value1} onChange={(e)=>handleChangeHeight(e,"heightA")} InputProps={{endAdornment: <InputAdornment position="start">m</InputAdornment>}}/>
          </Tooltip>
          </div>
        <div className={classes.flexContainer}>
          <Button className={classes.button} variant="contained" onClick={() => { onEnd(pos); props.onHeightB(height!,+value2);}}>End</Button>
          <Tooltip title="Please add the antenna height in meters above sea level.">
          <TextField className={classes.textField} value={value2} onChange={(e)=>handleChangeHeight(e,"heightB")} InputProps={{endAdornment: <InputAdornment position="start">m</InputAdornment>}}/>
          </Tooltip>
          </div>
        </div>
        
      </div>
    );
  };
  

  export default MapContextMenu;
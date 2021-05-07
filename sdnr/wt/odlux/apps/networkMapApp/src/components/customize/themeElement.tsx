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

import { Typography } from '@material-ui/core';
import * as React from 'react'


type props={
    color: string,
    text: string
};

const ThemeEntry = (props: props) =>{

    var circleStyle = {
        padding:10,
        margin:20,
        backgroundColor: props.color,
        borderRadius: "50%",
        width:10,
        height:10,
        left:0,
        top:0};

        return <div style={{display: 'flex', flexDirection:'row'}}>
            <div style={circleStyle} />
            <Typography variant="body1" style={{marginTop:24}}>{props.text}</Typography>
            </div>

}

export default ThemeEntry;
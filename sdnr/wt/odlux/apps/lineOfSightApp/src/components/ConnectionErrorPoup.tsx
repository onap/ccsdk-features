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

import { faExclamationTriangle } from "@fortawesome/free-solid-svg-icons"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"
import { Paper, Typography } from "@material-ui/core"
import * as React from "react"

type props = { reachable: boolean|null};


const ConnectionErrorPoup: React.FunctionComponent<props> = (props) => {

    return (props.reachable === false ?  
    <Paper style={{padding:5, position: 'absolute', top: 160, width: 230, left:"40%",  zIndex:1}}>
        <div style={{display: 'flex', flexDirection: 'column'}}>
        <div style={{'alignSelf': 'center', marginBottom:5}}> <Typography> <FontAwesomeIcon icon={faExclamationTriangle} /> Connection Error</Typography></div>
         <Typography>Service unavailable</Typography>
        </div>
    </Paper> : null
)

}

export default ConnectionErrorPoup;
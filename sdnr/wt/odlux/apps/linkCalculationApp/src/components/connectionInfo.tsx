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

import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import connect, { IDispatcher, Connect } from "../../../../framework/src/flux/connect";
import { Paper, Typography } from "@material-ui/core";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';


type props = Connect<typeof mapStateToProps, typeof mapDispatchToProps>;

const ConnectionInfo: React.FunctionComponent<props> = (props) => {

    return (
        (props.isCalculationServerReachable === false)?  <Paper style={{padding:5, width: 230, position:"absolute", top:"40%", left:"40%"}}>
        <div style={{display: 'flex', flexDirection: 'column'}}>
        <div style={{'alignSelf': 'center', marginBottom:5}}> <Typography> <FontAwesomeIcon icon={faExclamationTriangle} /> Connection Error</Typography></div>
        {props.isCalculationServerReachable === false && <Typography> Calculation data can't be loaded.</Typography>}
        </div>
    </Paper> : null
        
)}

const mapStateToProps = (state: IApplicationStoreState) => ({
    isCalculationServerReachable: state.linkCalculation.calculations.reachable
});



const mapDispatchToProps = (dispatcher: IDispatcher) => ({

    //zoomToSearchResult: (lat: number, lon: number) => dispatcher.dispatch(new ZoomToSearchResultAction(lat, lon))

});;


export default connect(mapStateToProps,mapDispatchToProps)(ConnectionInfo)


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
import { Paper, Typography, Tooltip } from '@material-ui/core';
import InfoIcon from '@material-ui/icons/Info';

import { IApplicationStoreState } from '../../../../../framework/src/store/applicationStore';
import connect, { IDispatcher, Connect } from '../../../../../framework/src/flux/connect';

type props = Connect<typeof mapStateToProps, typeof mapDispatchToProps>;

const mapStateToProps = (state: IApplicationStoreState) => ({
    linkCount: state.network.map.statistics.links,
    siteCount: state.network.map.statistics.sites,
    isTopoServerReachable: state.network.connectivity.isToplogyServerAvailable,
    isTileServerReachable: state.network.connectivity.isTileServerAvailable,

});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({
});

const Statistics: React.FunctionComponent<props> = (props: props) =>{

    const reachabe = props.isTopoServerReachable && props.isTileServerReachable;


    return (<Paper style={{ padding: 5, position: 'absolute', display: 'flex', flexDirection: "column", top: 70, width: 200, marginLeft: 5 }}>
    <div style={{ display: 'flex', flexDirection: "row" }}>
        <Typography style={{ fontWeight: "bold", flex: "1", color: reachabe ? "black" : "lightgrey" }} >Statistics</Typography>
        <Tooltip style={{ alignSelf: "flex-end" }} title="Gets updated when the map stops moving.">
            <InfoIcon fontSize="small" />
        </Tooltip>
    </div>

    <Typography style={{ color: reachabe ? "black" : "lightgrey" }}>Sites: {props.siteCount}</Typography>
    <Typography style={{ color: reachabe ? "black" : "lightgrey" }}>Links: {props.linkCount}</Typography>
</Paper>)
}

export default connect(mapStateToProps, mapDispatchToProps)(Statistics);

/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import React from 'react'
import {connect, Connect, IDispatcher } from "../../../../framework/src/flux/connect";

import { NavigateToApplication } from "../../../../framework/src/actions/navigationActions";
import { FunctionComponent } from "react";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import TocEntry from "../components/tocEntry";
import { Typography } from "@mui/material";

const mapProps = (state: IApplicationStoreState) => ({
    helpToc: state.help.toc,
})

const mapDisp = (dispatcher: IDispatcher) => ({
    requestDocument: (uri: string) => dispatcher.dispatch(new NavigateToApplication("help", uri))
});

const HelpTocComponent: FunctionComponent<Connect<typeof mapProps, typeof mapDisp>> = (props) => {

    return (
        <div>
            <Typography aria-label="help" style={{ marginBottom: '30px' }} variant="h5">
                Help &amp; FAQ
            </Typography>
            <Typography style={{ marginBottom: '30px' }} variant="body1">
                On our Help site, you can find general information about SDN-R, detailed information about our applications, frequently asked questions and a list of used abbreviations.
            </Typography>
            {
                props.helpToc && props.helpToc.map((item, index) => <TocEntry key={index} overviewUri={item.uri} nodes={item.nodes} label={item.label} loadDocument={props.requestDocument} />)
            }
        </div>
    )
}

export const HelpTocApp = connect(mapProps, mapDisp)(HelpTocComponent)

export default HelpTocApp;
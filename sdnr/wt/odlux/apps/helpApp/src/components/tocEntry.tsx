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

import * as React from "react"
import { TocTreeNode } from "../models/tocNode"
import { Typography, Link, Theme } from "@mui/material";

import makeStyles from '@mui/styles/makeStyles';
import createStyles from '@mui/styles/createStyles';

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        link: {
            color: "blue",
        },
        sublink: {
            margin: theme.spacing(1),
            color: "blue",
        },
        container: {
            display: "flex",
            flexDirection: "row",
            flexWrap: "wrap",
        }
    }),
);

type tocEntryProps = {
    label: string,
    overviewUri: string,
    nodes?: TocTreeNode[],
    loadDocument(uri: string): any
}

const TocEntry: React.FunctionComponent<tocEntryProps> = (props) => {
    const classes = useStyles();
    const areNodesEmpty = !props.nodes || props.nodes.length === 0

    const navigate = (event: React.SyntheticEvent, uri: string) => {
        event.preventDefault();
        event.stopPropagation();
        props.loadDocument(uri);
    }

    return (<div>
        {
            areNodesEmpty ? <Typography variant="h6">
                <Link underline="hover" onClick={(event: any) => navigate(event, props.overviewUri)} className={classes.link}> {props.label}</Link>
            </Typography> :
                <>
                    <Typography variant="h6">
                        {props.label}
                    </Typography>
                    <div className={classes.container}>
                        <Typography variant="body1">
                            <Link underline="hover" onClick={(event: any) => navigate(event, props.overviewUri)} className={classes.sublink}>Overview</Link>
                        </Typography>
                        {props.nodes !== undefined && props.nodes.map((item, index) =>
                            <Typography variant="body1" key={index + 'x' + item.id}>
                                <Link underline="hover" onClick={(event: any) => navigate(event, item.uri)} className={classes.sublink}>{item.label}</Link>
                            </Typography>
                        )}
                    </div>
                </>
        }
    </div >)
}


export default TocEntry;
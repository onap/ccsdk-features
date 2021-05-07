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
import { Typography, Select, MenuItem, ClickAwayListener, Popper, Paper, FormGroup, Portal, Popover } from '@material-ui/core';
import { SelectSiteAction, ClearHistoryAction, ClearDetailsAction } from '../../actions/detailsAction';
import { Site } from '../../model/site';
import { link } from '../../model/link';
import { URL_API } from '../../config';
import { HighlightLinkAction, HighlightSiteAction } from '../../actions/mapActions';
import { IApplicationStoreState } from '../../../../../framework/src/store/applicationStore';
import connect, { IDispatcher, Connect } from '../../../../../framework/src/flux/connect';
import { verifyResponse, handleConnectionError } from '../../actions/connectivityAction';




const MapPopup: React.FunctionComponent<props> = (props) => {

    const [value, setValue] = React.useState("");

    const handleChange = (event: any) => {
        setValue(event.target.value);

        const id = event.target.value;

       
        fetch(`${URL_API}/${props.type.toLocaleLowerCase()}s/${id}`)
        .then(result => verifyResponse(result))
        .then(res => res.json())
        .then(result => {
            props.clearDetailsHistory();
            props.selectElement(result);
            props.type === "link" ?  props.highlightLink(result)  : props.highlightSite(result)
            props.onClose();
        })
        .catch(error => {
            props.handleConnectionError(error); 
            props.onClose(); 
           // props.clearDetails();
        });
    };

    return <>
        <Popover open={true}  anchorEl={undefined} onClose={props.onClose} anchorReference="anchorPosition" anchorPosition={{ top: props.position.left, left: props.position.top }}>
            <Paper style={{ padding: "15px" }}>
                <Typography variant="h5">{`Multiple ${props.type.toLowerCase()}s were selected`}</Typography>
                <Typography variant="body1">Please select one.</Typography>
                <Select style={{ width: 300 }} onChange={handleChange} value={value} native>
                    <option value={""} disabled>{props.type} ids</option>
                    {
                        props.elements.map(el => <option key={el.id} value={el.id}>{el.name}</option>)
                    }
                </Select>
            </Paper>
        </Popover>
    </>
}

type props = Connect<typeof mapStateToProps, typeof mapDispatchToProps>& { onClose(): void }

const mapStateToProps = (state: IApplicationStoreState) => ({
    elements: state.network.popup.selectionPendingForElements,
    type: state.network.popup.pendingDataType,
    position: state.network.popup.position

});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({ 
    selectElement: (site: Site) => dispatcher.dispatch(new SelectSiteAction(site)),
    clearDetailsHistory:()=> dispatcher.dispatch(new ClearHistoryAction()),
    highlightLink: (link: link) => dispatcher.dispatch(new HighlightLinkAction(link)),
    highlightSite: (site: Site) => dispatcher.dispatch(new HighlightSiteAction(site)),
    handleConnectionError: (error:Error) => dispatcher.dispatch(handleConnectionError(error)),
    clearDetails: () => dispatcher.dispatch(new ClearDetailsAction()),

});

export default (connect(mapStateToProps, mapDispatchToProps))(MapPopup);
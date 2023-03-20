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
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, DialogContentText, Checkbox, Button, FormControlLabel, FormGroup } from '@mui/material';
import { IApplicationState } from '../../../../framework/src/handlers/applicationStateHandler';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { connect, Connect } from '../../../../framework/src/flux/connect';
import { MediatorConfigResponse } from '../models/mediatorServer';
import { Panel } from '../../../../framework/src/components/material-ui/panel';

export enum MediatorInfoDialogMode {
    None = "none",
    ShowDetails = "showDetails"
}

const mapProps = (state: IApplicationStoreState) => ({ supportedDevices: state.mediator.mediatorServerState.supportedDevices })

type ShowMediatorInfoDialogComponentProps = Connect<typeof mapProps, undefined> &
{
    config: MediatorConfigResponse,
    mode: MediatorInfoDialogMode,
    onClose: () => void
}

type ShowMediatorInfoDialogComponentState = {
    status: string,
    devicetype: string,
    activeOdlConfig: string
}

/*
Displays all values of a mediator server 
*/
class ShowMediatorInfoDialogComponent extends React.Component<ShowMediatorInfoDialogComponentProps, ShowMediatorInfoDialogComponentState> {

    constructor(props: ShowMediatorInfoDialogComponentProps) {
        super(props);
        if (this.props.config) {
            let deviceType = props.supportedDevices.find(element => element.id === this.props.config.DeviceType)

            this.state = {
                status: props.config.pid > 0 ? "Running" : "Stopped",
                devicetype: deviceType != undefined ? deviceType.device : 'none',
                activeOdlConfig: ''
            }
        }
    }

    onClose = (event: React.MouseEvent) => {
        event.preventDefault();
        event.stopPropagation();
        this.props.onClose();
    }

    render() {
        return (
            <Dialog open={this.props.mode !== MediatorInfoDialogMode.None} onBackdropClick={this.props.onClose} >
                <DialogTitle>{this.props.config.Name}</DialogTitle>
                <DialogContent>
                    <TextField variant="standard" disabled margin="dense" id="deviceIp" label="Device IP" fullWidth defaultValue={this.props.config.DeviceIp} />
                    <TextField variant="standard" disabled margin="dense" id="deviceport" label="Device Port" fullWidth defaultValue={this.props.config.DevicePort} />
                    <TextField variant="standard" disabled margin="dense" id="status" label="Status" fullWidth defaultValue={this.state.status} />
                    <TextField variant="standard" disabled margin="dense" id="deviceType" label="Device Type" fullWidth defaultValue={this.state.devicetype} />
                    <TextField variant="standard" disabled margin="dense" id="ncPort" label="Netconf Port" fullWidth defaultValue={this.props.config.NcPort} />
                    <FormGroup>
                        <FormControlLabel control={<Checkbox disabled defaultChecked={this.props.config.IsNCConnected}></Checkbox>} label="Netconf Connection" />
                        <FormControlLabel control={<Checkbox disabled defaultChecked={this.props.config.IsNeConnected}></Checkbox>} label="Network Element Connection" />
                        <FormControlLabel control={<Checkbox disabled defaultChecked={this.props.config.fwactive}></Checkbox>} label="Firewall active" />
                    </FormGroup>
                    {
                        this.props.config.ODLConfig.map((element, index) =>
                            <Panel title={"ODL config " + (this.props.config.ODLConfig.length > 1 ? index + 1 : '')} key={index} panelId={'panel-' + index} activePanel={this.state.activeOdlConfig} onToggle={(id: string) => { this.setState({ activeOdlConfig: (this.state.activeOdlConfig === id) ? "" : (id || "") }); }}>
                                <TextField variant="standard" disabled margin="dense" defaultValue={element.Protocol + '://' + element.Server} label="Server" />
                                <TextField variant="standard" disabled margin="dense" defaultValue={element.Port} label="Port" />
                                <FormControlLabel control={<Checkbox disabled checked={element.Trustall} />} label="Trustall" />
                            </Panel>
                        )
                    }

                </DialogContent>
                <DialogActions>
                    <Button onClick={this.onClose} color="inherit">Close</Button>
                </DialogActions>
            </Dialog>
        )
    }

}

export const ShowMediatorInfoDialog = connect(mapProps)(ShowMediatorInfoDialogComponent)
export default ShowMediatorInfoDialog;
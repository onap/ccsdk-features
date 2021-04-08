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
import * as React from 'react';

import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';

import { currentProblemsReloadAction } from '../handlers/currentProblemsHandler';
import { IDispatcher, connect, Connect } from '../../../../framework/src/flux/connect';

import { Fault } from '../models/fault';

export enum RefreshCurrentProblemsDialogMode {
    None = "none",
    RefreshCurrentProblemsTable = "RefreshCurrentProblemsTable",
}

const mapDispatch = (dispatcher: IDispatcher) => ({
    refreshCurrentProblems: () => dispatcher.dispatch(currentProblemsReloadAction)
});

type DialogSettings = {
    dialogTitle: string,
    dialogDescription: string,
    applyButtonText: string,
    cancelButtonText: string,
    enableMountIdEditor: boolean,
    enableUsernameEditor: boolean,
    enableExtendedEditor: boolean,
}

const settings: { [key: string]: DialogSettings } = {
    [RefreshCurrentProblemsDialogMode.None]: {
        dialogTitle: "",
        dialogDescription: "",
        applyButtonText: "",
        cancelButtonText: "",
        enableMountIdEditor: false,
        enableUsernameEditor: false,
        enableExtendedEditor: false,
    },
    [RefreshCurrentProblemsDialogMode.RefreshCurrentProblemsTable]: {
        dialogTitle: "Do you want to refresh the Current Problems List?",
        dialogDescription: "",
        applyButtonText: "Yes",
        cancelButtonText: "Cancel",
        enableMountIdEditor: true,
        enableUsernameEditor: true,
        enableExtendedEditor: true,
    }
}

type RefreshCurrentProblemsDialogComponentProps = Connect<undefined, typeof mapDispatch> & {
    mode: RefreshCurrentProblemsDialogMode;
    onClose: () => void;
};

type RefreshCurrentProblemsDialogComponentState = Fault & { isNameValid: boolean, isHostSet: boolean };

class RefreshCurrentProblemsDialogComponent extends React.Component<RefreshCurrentProblemsDialogComponentProps, RefreshCurrentProblemsDialogComponentState> {
    constructor(props: RefreshCurrentProblemsDialogComponentProps) {
        super(props);
    }

    render(): JSX.Element {
        const setting = settings[this.props.mode];
        return (
            <Dialog open={this.props.mode !== RefreshCurrentProblemsDialogMode.None}>
                <DialogTitle id="form-dialog-title" aria-label={`${setting.dialogTitle.replace(/ /g, "-").toLowerCase()}-dialog`}>{setting.dialogTitle}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {setting.dialogDescription}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button aria-label="dialog-confirm-button" onClick={(event) => {
                        this.onRefresh();
                    }} > {setting.applyButtonText} </Button>
                    <Button aria-label="dialog-cancel-button" onClick={(event) => {
                        this.onCancel();
                    }} color="secondary"> {setting.cancelButtonText} </Button>
                </DialogActions>
            </Dialog>
        )
    }

    private onRefresh = () => {
        this.props.refreshCurrentProblems();
        this.props.onClose();
    };

    private onCancel = () => {
        this.props.onClose();
    }
}

export const RefreshCurrentProblemsDialog = connect(undefined, mapDispatch)(RefreshCurrentProblemsDialogComponent);
export default RefreshCurrentProblemsDialog;
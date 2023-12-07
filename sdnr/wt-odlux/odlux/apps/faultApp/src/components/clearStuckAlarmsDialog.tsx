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

import React from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';

import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { clearStuckAlarmAsyncAction } from '../actions/clearStuckAlarmsAction';
import { currentAlarmsReloadAction } from '../handlers/currentAlarmsHandler';

export enum ClearStuckAlarmsDialogMode {
  None = 'none',
  Show = 'show',
}

const mapDispatch = (dispatcher: IDispatcher) => ({
  clearStuckAlarmsAsync: clearStuckAlarmAsyncAction(dispatcher.dispatch),
  reloadCurrentAlarmsAction: () => dispatcher.dispatch(currentAlarmsReloadAction),
});

type clearStuckAlarmsProps = Connect<typeof undefined, typeof mapDispatch> & {
  numberDevices: Number;
  mode: ClearStuckAlarmsDialogMode;
  stuckAlarms: string[];
  onClose: () => void;
};

type ClearStuckAlarmsState = {
  clearAlarmsSuccessful: boolean;
  errormessage: string;
  unclearedAlarms: string[];
};

class ClearStuckAlarmsDialogComponent extends React.Component<clearStuckAlarmsProps, ClearStuckAlarmsState> {
  constructor(props: clearStuckAlarmsProps) {
    super(props);
    this.state = {
      clearAlarmsSuccessful: true,
      errormessage: '',
      unclearedAlarms: [],
    };
  }

  onClose = (event: React.MouseEvent) => {
    event.stopPropagation();
    event.preventDefault();
    this.props.onClose();
  };

  onRefresh = async (event: React.MouseEvent) => {
    event.stopPropagation();
    event.preventDefault();
    const result = await this.props.clearStuckAlarmsAsync(this.props.stuckAlarms);

    if (result && result['devicemanager:output'].nodenames && result['devicemanager:output'].nodenames.length !== this.props.stuckAlarms.length) { //show errormessage if not all devices were cleared
      const undeletedAlarm = this.props.stuckAlarms.filter(item => !result['devicemanager:output'].nodenames.includes(item));
      const error = 'The alarms of the following devices couldn\'t be refreshed: ';
      this.setState({ clearAlarmsSuccessful: false, errormessage: error, unclearedAlarms: undeletedAlarm });
      return;

    } else { //show errormessage if no devices were cleared
      this.setState({ clearAlarmsSuccessful: false, errormessage: 'Alarms couldn\'t be refreshed.', unclearedAlarms: [] });
    }

    this.props.reloadCurrentAlarmsAction();
    this.props.onClose();
  };

  onOk = (event: React.MouseEvent) => {

    event.stopPropagation();
    event.preventDefault();
    if (this.state.unclearedAlarms.length > 0)
      this.props.reloadCurrentAlarmsAction();
    this.props.onClose();
  };

  render() {
    console.log(this.props.stuckAlarms);
    const device = this.props.numberDevices > 1 ? 'devices' : 'device';
    const defaultMessage = 'Are you sure you want to refresh all alarms for ' + this.props.numberDevices + ' ' + device + '?';
    const message = this.state.clearAlarmsSuccessful ? defaultMessage : this.state.errormessage;

    const defaultTitle = 'Refresh Confirmation';
    const title = this.state.clearAlarmsSuccessful ? defaultTitle : 'Refresh Result';

    return (
      <Dialog open={this.props.mode !== ClearStuckAlarmsDialogMode.None}>
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {message}
          </DialogContentText>
          {
            this.state.unclearedAlarms.map(item =>
              <DialogContentText>
                {item}
              </DialogContentText>,
            )
          }
        </DialogContent>
        <DialogActions>
          {
            this.state.clearAlarmsSuccessful &&
            <>
              <Button color="inherit" onClick={this.onRefresh}>Yes</Button>
              <Button color="inherit" onClick={this.onClose}>No</Button>
            </>
          }

          {
            !this.state.clearAlarmsSuccessful && <Button color="inherit" onClick={this.onOk}>Ok</Button>
          }
        </DialogActions>
      </Dialog>
    );
  }
}

const ClearStuckAlarmsDialog = connect(undefined, mapDispatch)(ClearStuckAlarmsDialogComponent);
export default ClearStuckAlarmsDialog;

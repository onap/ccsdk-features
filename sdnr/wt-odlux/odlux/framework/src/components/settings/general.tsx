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

import  React from 'react';
import { Button, FormControlLabel, Switch, Typography } from '@mui/material';
import makeStyles from '@mui/styles/makeStyles';
import { SettingsComponentProps } from '../../models/settings';
import { connect, Connect, IDispatcher } from '../../flux/connect';
import { IApplicationStoreState } from '../../store/applicationStore';
import { getGeneralSettingsAction, SetGeneralSettingsAction, updateGeneralSettingsAction } from '../../actions/settingsAction';
import { sendMessage, SettingsMessage } from '../../services/broadcastService';


type props = Connect<typeof mapProps, typeof mapDispatch> & SettingsComponentProps;

const mapProps = (state: IApplicationStoreState) => ({
    settings: state.framework.applicationState.settings,
    user: state.framework.authenticationState.user?.user
    
});

const mapDispatch = (dispatcher: IDispatcher) => ({

    updateSettings :(activateNotifications: boolean) => dispatcher.dispatch(updateGeneralSettingsAction(activateNotifications)),
    getSettings: () =>dispatcher.dispatch(getGeneralSettingsAction()),
  });

const styles = makeStyles({
    sectionMargin: {
      marginTop: "30px",
      marginBottom: "15px"
    },
    elementMargin: {
      marginLeft: "10px"
    },
    buttonPosition:{
      position: "absolute",
      right: "32%"
    }
  });

const General : React.FunctionComponent<props> = (props) =>{

const classes = styles();

const [areWebsocketsEnabled, setWebsocketsEnabled] = React.useState(props.settings.general.areNotificationsEnabled || false);

React.useEffect(()=>{
  props.getSettings();
},[]);

React.useEffect(()=>{
  if(props.settings.general.areNotificationsEnabled!==null)
    setWebsocketsEnabled(props.settings.general.areNotificationsEnabled)
},[props.settings]);

const onWebsocketsChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>, newValue: boolean) =>{
    setWebsocketsEnabled(newValue);
  }

const onSave = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) =>{

    e.preventDefault();
    const message: SettingsMessage = {key: 'general', enableNotifications: areWebsocketsEnabled, user: props.user!};
    sendMessage(message, "odlux_settings");
    props.updateSettings(areWebsocketsEnabled);
    props.onClose();
}

const onCancel = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) =>{
  e.preventDefault();
  props.onClose();

}


    return <div>
        <Typography className={classes.sectionMargin} variant="body1" style={{ fontWeight: "bold" }} gutterBottom>
          Enable Notifications
        </Typography>
        <FormControlLabel style={{ padding:5}}
        value="end"
        control={<Switch color="secondary" aria-label="enable-notifications-button" aria-checked={areWebsocketsEnabled} checked={areWebsocketsEnabled} onChange={onWebsocketsChange} />}
        label="Enable Notifications"
        labelPlacement="end"
      />
      <div className={classes.buttonPosition}>
       <Button aria-label="cancel-button" className={classes.elementMargin} variant="contained" color="primary" onClick={onCancel}>Cancel</Button>
       <Button aria-label="save-button" className={classes.elementMargin} variant="contained" color="secondary" onClick={onSave}>Save</Button>
    </div>
    </div>
}

export const GeneralUserSettings = connect(mapProps, mapDispatch)(General);
export default GeneralUserSettings;
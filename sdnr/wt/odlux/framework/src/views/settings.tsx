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

import React, {FC, useState } from 'react';
import { useApplicationDispatch } from "../flux/connect";

import { Divider, List, ListItem, ListItemText, Paper } from '@mui/material';
import { makeStyles } from '@mui/styles';
import applicationService from '../services/applicationManager';

import { GoBackAction } from '../actions/navigationActions';
import { GeneralUserSettings } from '../components/settings/general';
import { toAriaLabel } from '../utilities/yangHelper';

type SettingsEntry = { name: string, element: JSX.Element };

const styles = makeStyles({
  sectionMargin: {
    marginTop: "30px",
    marginBottom: "15px"
  },
  elementMargin: {
    marginLeft: "10px"
  },
  menu: {
    flex: "1 0 0%",
  }
});

const UserSettings: FC = (props) => {

  const dispatch = useApplicationDispatch();
  const goBack = () => dispatch(new GoBackAction());

  const [selectedIndex, setSelectedIndex] = useState(0);

  const registrations = applicationService.applications;

  const navigateBack = () => {
    goBack();
  }

  let settingsArray: SettingsEntry[] = [];

  //add all framework specific settings
  settingsArray.push({name:"General", element: <GeneralUserSettings onClose={navigateBack} />})

  //get app settings
  let settingsElements : (SettingsEntry) [] = Object.keys(registrations).map(p => {
    const application = registrations[p];

    if (application.settingsElement) {
      const value: SettingsEntry = { name: application.menuEntry?.toString()!, element: <application.settingsElement onClose={navigateBack} /> };
      return value;

    } else {
      return null;
    }
  }).filter((x): x is SettingsEntry => x !== null);


  settingsArray.push(...settingsElements);

  const onSelectElement = (e: any, newValue: number) => {
    e.preventDefault();
    setSelectedIndex(newValue);
  }

  const classes = styles();

  return <div style={{ display: "flex", flexDirection: "row", height: "100%" }}>
   <div style={{ display: "flex", flexDirection: "column", height: "100%", width: "15%" }}>
      <Paper variant="outlined" style={{ height: "70%" }}>
        <List className={classes.menu} component="nav">
          {
            settingsArray.map((el, index) => {
              return (
              <>
                <ListItem key={"settings-key-"+index} selected={selectedIndex === index} button onClick={e => { onSelectElement(e, index) }} aria-label={toAriaLabel(el?.name+"-settings")}>
                  <ListItemText primary={el?.name} style={{ padding: 0 }} />
                </ListItem>
                <Divider />
              </>)
            })
          }
        </List>
      </Paper>
    </div>
    <div style={{ height: "100%", width: "80%", marginLeft: 15 }}>
      <div style={{ height: "100%" }}>
        {
            settingsArray[selectedIndex]?.element
        }
      </div>
    </div>
  </div>
}


export default UserSettings;
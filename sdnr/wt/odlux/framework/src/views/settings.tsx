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

import * as React from 'react';
import { IApplicationStoreState } from "../store/applicationStore";
import connect, { Connect, IDispatcher } from "../flux/connect";

import applicationService from '../services/applicationManager';
import { makeStyles } from '@material-ui/styles';
import { Divider, List, ListItem, ListItemText, Paper } from '@material-ui/core';

import { GeneralUserSettings } from '../components/settings/general'
import { GoBackAction } from '../actions/navigationActions';
import { toAriaLabel } from '../utilities/yangHelper';

type props = Connect<typeof mapProps, typeof mapDispatch>;

type SettingsEntry = { name: string, element: JSX.Element }


const mapProps = (state: IApplicationStoreState) => ({

});

const mapDispatch = (dispatcher: IDispatcher) => ({
  goBack: () => dispatcher.dispatch(new GoBackAction())
});

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

const UserSettings: React.FunctionComponent<props> = (props) => {

  const classes = styles();
  const registrations = applicationService.applications;

  const [selectedIndex, setSelectedIndex] = React.useState(0);

  const navigateBack = () => {
    props.goBack();
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

  return <div style={{ display: "flex", flexDirection: "row", height: "100%" }}>
   <div style={{ display: "flex", flexDirection: "column", height: "100%", width: "15%" }}>
      <Paper variant="outlined" style={{ height: "70%" }}>
        <List className={classes.menu} component="nav">
          {
            settingsArray.map((el, index) => {
              return (
              <>
                <ListItem selected={selectedIndex === index} button onClick={e => { onSelectElement(e, index) }} aria-label={toAriaLabel(el?.name+"-settings")}>
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


export default connect(mapProps, mapDispatch)(UserSettings);

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

import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons'; // select app icon
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Typography from '@mui/material/Typography';
import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';

import { connect, Connect } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';


const styles = () => createStyles({
  icon: {
    marginLeft: 8,
    marginRight: 8,
  },
  critical: {
    color: 'red',
  },
  major: {
    color: 'orange',
  },
  minor: {
    color: '#f7f700',
  },
  warning: {
    color: '#428bca',
  },
});

const mapProps = (state: IApplicationStoreState) => ({
  faultStatus: state.fault.faultStatus,
});


type FaultStatusComponentProps = & WithStyles<typeof styles> & Connect<typeof mapProps>;

class FaultStatusComponent extends React.Component<FaultStatusComponentProps> {
  render(): JSX.Element {
    const { classes, faultStatus } = this.props;

    return (
      <>
      <Typography variant="body1" color="inherit" aria-label="critical-alarms">
        Alarm Status: <FontAwesomeIcon className={`${classes.icon} ${classes.critical}`} icon={faExclamationTriangle} /> { faultStatus.critical  } |
        </Typography>
        <Typography variant="body1" color="inherit" aria-label="major-alarms">
        <FontAwesomeIcon className={`${classes.icon} ${classes.major}`} icon={faExclamationTriangle} /> { faultStatus.major } |
        </Typography>
        <Typography variant="body1" color="inherit" aria-label="minor-alarms">
        <FontAwesomeIcon className={`${classes.icon} ${classes.minor}`} icon={faExclamationTriangle} /> { faultStatus.minor } |
        </Typography>
        <Typography variant="body1" color="inherit" aria-label="warning-alarms">
        <FontAwesomeIcon className={`${classes.icon} ${classes.warning}`} icon={faExclamationTriangle} /> { faultStatus.warning } |
      </Typography>
      </>
    );
  }
}

export const FaultStatus = withStyles(styles)(connect(mapProps)(FaultStatusComponent));
export default FaultStatus;
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

import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';  // select app icon

import connect, { Connect } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import Typography from '@material-ui/core/Typography';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const styles = (theme: Theme) => createStyles({
  icon: {
    marginLeft: 8,
    marginRight: 8
  },
  critical: {
    color: "red"
  },
  major: {
    color: "orange"
  },
  minor: {
    color: "#f7f700"
  },
  warning: {
    color: "#428bca"
  }
});

const mapProps = (state: IApplicationStoreState) => ({
  faultStatus: state.fault.faultStatus,
});


type FaultStatusComponentProps = & WithStyles<typeof styles> & Connect<typeof mapProps>;

class FaultStatusComponent extends React.Component<FaultStatusComponentProps> {
  render(): JSX.Element {
    const { classes, faultStatus } = this.props;

    return (
      <Typography variant="body1" color="inherit" >
        Alarm status: <FontAwesomeIcon className={`${classes.icon} ${classes.critical}`} icon={faExclamationTriangle} /> { faultStatus.critical  } |
        <FontAwesomeIcon className={`${classes.icon} ${classes.major}`} icon={faExclamationTriangle} /> { faultStatus.major } |
        <FontAwesomeIcon className={`${classes.icon} ${classes.minor}`} icon={faExclamationTriangle} /> { faultStatus.minor } |
        <FontAwesomeIcon className={`${classes.icon} ${classes.warning}`} icon={faExclamationTriangle} /> { faultStatus.warning } |
      </Typography>
    );
  };
}

export const FaultStatus = withStyles(styles)(connect(mapProps)(FaultStatusComponent));
export default FaultStatus;
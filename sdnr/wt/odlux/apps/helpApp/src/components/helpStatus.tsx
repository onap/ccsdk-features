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
import { withRouter, RouteComponentProps } from 'react-router';

import { Theme } from '@mui/material/styles';
import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';  // select app icon

import Typography from '@mui/material/Typography';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faQuestionCircle } from '@fortawesome/free-solid-svg-icons';

import { connect, Connect } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

const styles = (theme: Theme) => createStyles({
  icon: {
    marginLeft: 8,
    marginRight: 8
  },
  disabled: {
    color: theme.palette.grey[400]
  },
  link: {
    cursor: "pointer",
    '&:hover': {
      textDecoration: "underline"
    }
  }
});

const mapProps = (state: IApplicationStoreState) => ({
  appId: state.framework.applicationState.appId,
  toc: state.help.toc
});


type HelpStatusComponentProps = & RouteComponentProps & WithStyles<typeof styles> & Connect<typeof mapProps>;

class HelpStatusComponent extends React.Component<HelpStatusComponentProps> {
  render() {
    const { classes, history, toc, appId } = this.props;
    const rootNode = toc && toc.find(t => t.id === "sdnr");
    const helpNode = appId
      ? rootNode && rootNode.nodes && rootNode.nodes.find(n => n.id === appId || n.id === appId + "App")
      : rootNode;
    return helpNode
      ? (
        <Typography variant="body1" color="inherit" className={classes.link} onClick={(event) => { event.stopPropagation(); history.push(`/help/${helpNode.uri}`) }} >
          <FontAwesomeIcon className={classes.icon} icon={faQuestionCircle} />
          Help
        </Typography>
      )
      : (
        <Typography variant="body1" className={classes.disabled}>
          <FontAwesomeIcon className={classes.icon} icon={faQuestionCircle} />
          Help
        </Typography>
      );
  };

}

export const HelpStatus = withRouter(withStyles(styles)(connect(mapProps)(HelpStatusComponent) as any) as any);
export default HelpStatus;
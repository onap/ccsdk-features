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
import  React from 'react';
import { Theme } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

import Modal from '@mui/material/Modal';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';

import { ClearErrorInfoAction, RemoveErrorInfoAction } from '../actions/errorActions';

import { connect, Connect } from '../flux/connect';

const styles = (theme: Theme) => createStyles({
  modal: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  paper: {
    width: theme.spacing(50),
    backgroundColor: theme.palette.background.paper,
    boxShadow: theme.shadows[5],
    padding: theme.spacing(4),
  },
  card: {
    minWidth: 275,
  },
  bullet: {
    display: 'inline-block',
    margin: '0 2px',
    transform: 'scale(0.8)',
  },
  title: {
    marginBottom: 16,
    fontSize: 14,
  },
  pos: {
    marginBottom: 12,
  },
});

type ErrorDisplayProps = WithStyles<typeof styles> & Connect;

// function getModalStyle() {
//   const top = 50 + rand();
//   const left = 50 + rand();

//   return {
//     top: `${ top }%`,
//     left: `${ left }%`,
//     transform: `translate(-${ top }%, -${ left }%)`,
//   };
// }

/**
 * Represents a component for formatting and displaying errors.
 */
class ErrorDisplayComponent extends React.Component<ErrorDisplayProps> {
  constructor(props: ErrorDisplayProps) {
    super(props);
  }

  render(): JSX.Element {
    const { classes, state } = this.props;
    const errorInfo = state.framework.applicationState.errors.length && state.framework.applicationState.errors[state.framework.applicationState.errors.length - 1];
    
    return (
      <Modal className={classes.modal}
        aria-labelledby="simple-modal-title"
        aria-describedby="simple-modal-description"
        open={state.framework.applicationState.errors && state.framework.applicationState.errors.length > 0}
        onClose={() => this.props.dispatch(new ClearErrorInfoAction())}
      >
        {errorInfo &&
          <div className={classes.paper}>
            <Card className={classes.card}>
              <CardContent>
                <Typography className={classes.title} color="textSecondary">
                  {errorInfo.title != null ? errorInfo.title : "Something went wrong."}
                </Typography>
                <Typography variant="h5" component="h2">
                  {errorInfo.error && errorInfo.error.toString()}
                </Typography>
                <Typography className={classes.pos} color="textSecondary">
                  {errorInfo.message && errorInfo.message.toString()}
                </Typography>
                <Typography component="p">
                  {errorInfo.info && errorInfo.info.componentStack && errorInfo.info.componentStack.split('\n').map(line => {
                    return [line, <br />];
                  })}
                  {errorInfo.info && errorInfo.info.extra && errorInfo.info.extra.split('\n').map(line => {
                    return [line, <br />];
                  })}
                </Typography>
              </CardContent>
              <CardActions>
                <Button color="inherit" size="small" onClick={() => this.props.dispatch(new RemoveErrorInfoAction(errorInfo))} >Close</Button>
              </CardActions>
            </Card>
          </div> || <div></div>
        }
      </Modal>
    );
  }
}

export const ErrorDisplay = withStyles(styles)(connect()(ErrorDisplayComponent));
export default ErrorDisplay;
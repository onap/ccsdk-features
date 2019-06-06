import * as React from 'react';

import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';  // select app icon

import connect, { Connect } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import Typography from '@material-ui/core/Typography';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faQuestionCircle } from '@fortawesome/free-solid-svg-icons';
import { withRouter, RouteComponentProps } from 'react-router';

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
      ? rootNode && rootNode.nodes && rootNode.nodes.find(n => n.id === appId || n.id === appId+"App")
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

export const HelpStatus = withRouter(withStyles(styles)(connect(mapProps)(HelpStatusComponent)));
export default HelpStatus;
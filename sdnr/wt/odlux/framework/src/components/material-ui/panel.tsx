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

import { Theme } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

import { Accordion, AccordionSummary, AccordionDetails, Typography, AccordionActions } from '@mui/material';

import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { SvgIconProps } from '@mui/material/SvgIcon';

const styles = (theme: Theme) => createStyles({
  accordion: {
    // background: theme.palette.secondary.dark,
    // color: theme.palette.primary.contrastText
  },
  detail: {
    // background: theme.palette.background.paper,
    // color: theme.palette.text.primary,
    position: "relative",
    flexDirection: 'column'
  },
  text: {
    // color: theme.palette.common.white,
    // fontSize: "1rem"
  },
});

type PanalProps = WithStyles<typeof styles> & {
  activePanel: string | null,
  panelId: string,
  title: string,
  customActionButtons?: JSX.Element[];
  onToggle: (panelId: string | null) => void;
}

const PanelComponent: React.SFC<PanalProps> = (props) => {
  const { classes, activePanel, onToggle } = props;
  return (
    <Accordion className={classes.accordion} expanded={activePanel === props.panelId} onChange={() => onToggle(props.panelId)} >
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography className={classes.text} >{props.title}</Typography>
      </AccordionSummary>
      <AccordionDetails className={classes.detail}>
        {props.children}
      </AccordionDetails>
      {props.customActionButtons
        ? <AccordionActions>
          {props.customActionButtons}
        </AccordionActions>
        : null}
    </Accordion>
  );
};

export const Panel = withStyles(styles)(PanelComponent);
export default Panel;
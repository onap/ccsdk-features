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


import * as React from "react";

import { Theme } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

const styles = (theme: Theme) => createStyles({
  "@keyframes spin": {
    "0%": { transform: "rotate(0deg)" },
    "100%": { transform: "rotate(360deg)" },
  },
  loader: {
    border: `16px solid ${theme.palette.grey.A200}`,
    borderTop: `16px solid ${theme.palette.secondary.main}`,
    borderRadius: "50%",
    width: "120px",
    height: "120px",
    animation: "$spin 2s linear infinite",
  }
});

const LoaderComponent: React.FC<WithStyles<typeof styles>> = (props) => {
  return (
    <div className={props.classes.loader} />
  );
};

export const Loader = withStyles(styles)(LoaderComponent);

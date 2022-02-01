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
import classNames from 'classnames';
import { Theme } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

export const styles = (theme: Theme) => createStyles({
  /* Styles applied to the root element. */
  root: { 
    transition: theme.transitions.create('background,box-shadow'),
    background: 'transparent',
    borderRadius: 2,
    overflow: 'hidden',
  },
  /* Styles applied to the root element if `selected={true}` or `selected="auto" and `value` set. */
  selected: {
    background: theme.palette.background.paper,
    boxShadow: theme.shadows[2],
  },
});


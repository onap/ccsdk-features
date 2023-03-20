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
import { RouteComponentProps, withRouter } from 'react-router-dom';

import Button from '@mui/material/Button';
import { Theme } from '@mui/material/styles'; // infra for styling
import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';

const styles = (theme: Theme) => createStyles({
  warnButton: {
    backgroundColor: theme.palette.primary.dark,
  },
});

type DetailProps = RouteComponentProps<{ id: string }> & WithStyles<typeof styles>;

export const Detail = withStyles( styles )( withRouter( (props: DetailProps) => (
  <div>
    <h1>Detail {props.match.params.id}</h1>
    <p>This are the information about {props.staticContext}.</p>
    <Button color={'secondary'} variant={'contained'}>Start</Button>
    <Button color="inherit" className={ props.classes.warnButton } variant={'contained'}>Stop</Button>
  </div>
)));

export default Detail;
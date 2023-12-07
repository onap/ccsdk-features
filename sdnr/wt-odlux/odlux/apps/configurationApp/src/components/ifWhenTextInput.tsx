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
import InputAdornment from '@mui/material/InputAdornment';
import Input, { InputProps } from '@mui/material/Input';
import Tooltip from '@mui/material/Tooltip';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import FormHelperText from '@mui/material/FormHelperText';

import makeStyles from '@mui/styles/makeStyles';
import createStyles from '@mui/styles/createStyles';

import { faAdjust } from '@fortawesome/free-solid-svg-icons/faAdjust';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { ViewElementBase } from '../models/uiModels';

const useStyles = makeStyles(() =>
  createStyles({
    iconDark: {
      color: '#ff8800',
    },
    iconLight: {
      color: 'orange',
    },
    padding: {
      paddingLeft: 10,
      paddingRight: 10,
    },
  }),
);

type IfWhenProps = InputProps & {
  label: string;
  element: ViewElementBase;
  helperText: string;
  error: boolean;
  onChangeTooltipVisibility(value: boolean): void;
};

export const IfWhenTextInput = (props: IfWhenProps) => {

  const { element, id, label, helperText: errorText, error, style, ...otherProps } = props;
  const classes = useStyles();

  const ifFeature = element.ifFeature
    ? (
      <Tooltip
        title={element.ifFeature}
        disableInteractive
        onMouseMove={() => props.onChangeTooltipVisibility(false)}
        onMouseOut={() => props.onChangeTooltipVisibility(true)}
      >
          <InputAdornment position="start">
            <FontAwesomeIcon icon={faAdjust} className={classes.iconDark} />
          </InputAdornment>
        </Tooltip>
    )
    : null;

  const whenFeature = element.when
    ? (
      <Tooltip
        title={element.when}
        disableInteractive
        className={classes.padding}
        onMouseMove={() => props.onChangeTooltipVisibility(false)}
        onMouseOut={() => props.onChangeTooltipVisibility(true)}
      >
          <InputAdornment className={classes.padding} position="end">
            <FontAwesomeIcon icon={faAdjust} className={classes.iconLight}/>
          </InputAdornment>
        </Tooltip>
    ) 
    : null;

  return (
    <FormControl variant="standard" error={error} style={style}>
      <InputLabel htmlFor={id} >{label}</InputLabel>
      <Input id={id} inputProps={{ 'aria-label': label + '-input' }} endAdornment={<div>{ifFeature}{whenFeature}</div>} {...otherProps}  />
      <FormHelperText>{errorText}</FormHelperText>
    </FormControl>
  );
};
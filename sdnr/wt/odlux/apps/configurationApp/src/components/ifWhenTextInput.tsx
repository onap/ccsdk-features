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

import { ViewElementBase } from "models/uiModels";
import { TextField, InputAdornment, Input, Tooltip, Divider, IconButton, InputBase, Paper, makeStyles, Theme, createStyles, FormControl, InputLabel, FormHelperText } from "@material-ui/core";
import * as React from 'react';
import { faAdjust } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { InputProps } from "@material-ui/core/Input";

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    iconDark: {
      color: '#ff8800'
    },
    iconLight: {
      color: 'orange'
    },
    padding: {
      paddingLeft: 10,
      paddingRight: 10
    },
  }),
);

type IfwhenProps = InputProps & {
  label: string;
  element: ViewElementBase;
  helperText: string;
  error: boolean;
  onChangeTooltipVisuability(value: boolean): void;
};

export const IfWhenTextInput = (props: IfwhenProps) => {

  const { element, onChangeTooltipVisuability: toogleTooltip, id, label, helperText: errorText, error, style, ...otherProps } = props;
  const classes = useStyles();


  const ifFeature = element.ifFeature
    ? (
        <Tooltip onMouseMove={e => props.onChangeTooltipVisuability(false)} onMouseOut={e => props.onChangeTooltipVisuability(true)} title={element.ifFeature}>
          <InputAdornment position="start">
            <FontAwesomeIcon icon={faAdjust} className={classes.iconDark} />
          </InputAdornment>
        </Tooltip>
      )
    : null;

  const whenFeature = element.when
    ? (
        <Tooltip className={classes.padding} onMouseMove={() => props.onChangeTooltipVisuability(false)} onMouseOut={() => props.onChangeTooltipVisuability(true)} title={element.when}>
          <InputAdornment className={classes.padding} position="end">
            <FontAwesomeIcon icon={faAdjust} className={classes.iconLight}/>
          </InputAdornment>
        </Tooltip>
      ) 
    : null;

  return (
    <FormControl error={error} style={style}>
      <InputLabel htmlFor={id} >{label}</InputLabel>
      <Input id={id} endAdornment={<div>{ifFeature}{whenFeature}</div>} {...otherProps}  />
      <FormHelperText>{errorText}</FormHelperText>
    </FormControl>
  );
}
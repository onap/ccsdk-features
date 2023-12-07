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

import React, { useState } from 'react';
import { Tooltip, Button, FormControl } from '@mui/material';

import createStyles from '@mui/styles/createStyles';
import makeStyles from '@mui/styles/makeStyles';

import { ViewElement } from '../models/uiModels';

const useStyles = makeStyles(() => createStyles({
  button: {
    'justifyContent': 'left',
  },
}));

type UIElementReferenceProps = {
  element: ViewElement;
  disabled: boolean;
  onOpenReference(element: ViewElement): void;
};

export const UIElementReference: React.FC<UIElementReferenceProps> = (props) => {
  const { element } = props;
  const [disabled, setDisabled] = useState(true);
  const classes = useStyles();
  return (
    <FormControl
      variant="standard"
      key={element.id}
      style={{ width: 485, marginLeft: 20, marginRight: 20 }}
      onMouseDown={(ev) => {
        ev.preventDefault();
        ev.stopPropagation();
        if (ev.button === 1) {
          setDisabled(!disabled);
        }
      }}>
      <Tooltip disableInteractive title={element.description || element.path || ''}>
        <Button
          className={classes.button}
          aria-label={element.label + '-button'}
          color="secondary"
          disabled={props.disabled && disabled}
          onClick={() => {
            props.onOpenReference(element);
          }}  >{`${element.label}`}</Button>
      </Tooltip>
    </FormControl>
  );
};
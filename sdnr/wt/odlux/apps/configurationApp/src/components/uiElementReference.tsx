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
import { Tooltip, Button, FormControl, Theme, createStyles, makeStyles } from '@material-ui/core';

import { ViewElement } from '../models/uiModels';

const useStyles = makeStyles((theme: Theme) => createStyles({
  button: {
    "justifyContent": "left"
  },
}));

type UIElementReferenceProps = {
  element: ViewElement;
  disabled: boolean;
  onOpenReference(element: ViewElement): void;
};

export const UIElementReference: React.FC<UIElementReferenceProps> = (props) => {
  const classes = useStyles();
  const { element } = props;
  return (
    <FormControl key={element.id} style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
      <Tooltip title={element.description || ''}>
        <Button className={classes.button} aria-label={element.label+'-button'} color="secondary" disabled={props.disabled} onClick={() => {
          props.onOpenReference(element);
        }}>{`${element.label}`}</Button>
      </Tooltip>
    </FormControl>
  );
}
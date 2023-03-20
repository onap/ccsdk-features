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
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Chip from '@mui/material/Chip';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';

import makeStyles from '@mui/styles/makeStyles';
import AddIcon from '@mui/icons-material/Add';

import { Theme } from '@mui/material/styles';
import { ViewElement } from '../models/uiModels';

import { BaseProps } from './baseProps';

const useStyles = makeStyles((theme: Theme) => {
  const light = theme.palette.mode === 'light';
  const bottomLineColor = light ? 'rgba(0, 0, 0, 0.42)' : 'rgba(255, 255, 255, 0.7)';

  return ({
    root: {
      display: 'flex',
      justifyContent: 'left',
      verticalAlign: 'bottom',
      flexWrap: 'wrap',
      listStyle: 'none',
      margin: 0,
      padding: 0,
      paddingTop: theme.spacing(0.5),
      marginTop: theme.spacing(1),
    },
    chip: {
      margin: theme.spacing(0.5),
    },
    underline: {
      '&:after': {
        borderBottom: `2px solid ${theme.palette.primary.main}`,
        left: 0,
        bottom: 0,
        // Doing the other way around crash on IE 11 "''" https://github.com/cssinjs/jss/issues/242
        content: '""',
        position: 'absolute',
        right: 0,
        transform: 'scaleX(0)',
        transition: theme.transitions.create('transform', {
          duration: theme.transitions.duration.shorter,
          easing: theme.transitions.easing.easeOut,
        }),
        pointerEvents: 'none', // Transparent to the hover style.
      },
      '&.Mui-focused:after': {
        transform: 'scaleX(1)',
      },
      '&.Mui-error:after': {
        borderBottomColor: theme.palette.error.main,
        transform: 'scaleX(1)', // error is always underlined in red
      },
      '&:before': {
        borderBottom: `1px solid ${bottomLineColor}`,
        left: 0,
        bottom: 0,
        // Doing the other way around crash on IE 11 "''" https://github.com/cssinjs/jss/issues/242
        content: '"\\00a0"',
        position: 'absolute',
        right: 0,
        transition: theme.transitions.create('border-bottom-color', {
          duration: theme.transitions.duration.shorter,
        }),
        pointerEvents: 'none', // Transparent to the hover style.
      },
      '&:hover:not($disabled):before': {
        borderBottom: `2px solid ${theme.palette.text.primary}`,
        // Reset on touch devices, it doesn't add specificity
        // eslint-disable-next-line @typescript-eslint/naming-convention
        '@media (hover: none)': {
          borderBottom: `1px solid ${bottomLineColor}`,
        },
      },
      '&.Mui-disabled:before': {
        borderBottomStyle: 'dotted',
      },
    },
  });
});

type LeafListProps = BaseProps<any []> & {
  getEditorForViewElement:  (uiElement: ViewElement) => (null | React.ComponentType<BaseProps<any>>);  
};

export const UiElementLeafList = (props: LeafListProps) => {
  const { value: element, inputValue, onChange } = props;

  const classes = useStyles();

  const [open, setOpen] = React.useState(false);
  const [editorValue, setEditorValue] = React.useState('');
  const [editorValueIndex, setEditorValueIndex] = React.useState(-1);
  
  const handleClose = () => {
    setOpen(false);
  };

  const onApplyButton = () => { 
    if (editorValue != null && editorValue != '' && editorValueIndex < 0) {
      props.onChange([
        ...inputValue,
        editorValue,
      ]);
    } else if (editorValue != null && editorValue != '') {
      props.onChange([
        ...inputValue.slice(0, editorValueIndex),
        editorValue,
        ...inputValue.slice(editorValueIndex + 1),
      ]);
    }
    setOpen(false);
  };

  const onDelete = (index : number) => {
    const newValue : any[] = [
      ...inputValue.slice(0, index),
      ...inputValue.slice(index + 1),
    ];
    onChange(newValue);
  };

  const ValueEditor = props.getEditorForViewElement(props.value); 

  return (
    <>
      <FormControl variant="standard" style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
        <InputLabel htmlFor={`list-${element.id}`} shrink={!props.readOnly || !!(inputValue && inputValue.length)} >{element.label}</InputLabel>
        <ul className={`${classes.root} ${classes.underline}`} id={`list-${element.id}`}>
        { !props.readOnly ? <li>
          <Chip
            icon={<AddIcon />}
            label={'Add'}
            className={classes.chip}
            size="small"
            color="secondary"
            onClick={ () => { 
              setOpen(true); 
              setEditorValue('');
              setEditorValueIndex(-1);
            } 
            }
          />
        </li> : null }  
        { inputValue.map((val, ind) => (
          <li key={ind}>
            <Chip
              className={classes.chip}
              size="small"
              variant="outlined"
              label={String(val)}
              onDelete={ !props.readOnly ? () => { onDelete(ind); } : undefined }  
              onClick={ !props.readOnly ? () => { 
                setOpen(true); 
                setEditorValue(val);
                setEditorValueIndex(ind);
              } : undefined
              }   
            />
            </li>
        ))
        }
        </ul>
        {/* <FormHelperText>{ "Value is mandetory"}</FormHelperText> */}
        </FormControl>
        <Dialog open={open} onClose={handleClose} aria-labelledby="form-dialog-title">
          <DialogTitle id="form-dialog-title">{editorValueIndex < 0 ? 'Add new value' : 'Edit value' } </DialogTitle>
          <DialogContent>
            { ValueEditor && <ValueEditor 
                inputValue={ editorValue }
                value={{ ...element, isList: false }}
                disabled={false}
                readOnly={props.readOnly}
                onChange={ setEditorValue }
            /> || null }
          </DialogContent>
          <DialogActions>
            <Button color="inherit" onClick={ handleClose }> Cancel </Button>
            <Button disabled={editorValue == null || editorValue === '' } onClick={ onApplyButton } color="secondary"> {editorValueIndex < 0 ? 'Add' : 'Apply'} </Button>
          </DialogActions>
        </Dialog>
      </>
  );
};
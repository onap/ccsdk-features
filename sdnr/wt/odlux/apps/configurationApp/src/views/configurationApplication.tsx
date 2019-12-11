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
import { RouteComponentProps, withRouter } from 'react-router-dom';

import { WithStyles, withStyles, createStyles, Theme } from '@material-ui/core/styles';

import connect, { IDispatcher, Connect } from "../../../../framework/src/flux/connect";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import MaterialTable, { ColumnModel, ColumnType, MaterialTableCtorType } from "../../../../framework/src/components/material-table";
import { Loader } from "../../../../framework/src/components/material-ui/loader";

import { SetSelectedValue, splitVPath, updateDataActionAsyncCreator } from "../actions/deviceActions";
import { ViewSpecification, isViewElementString, isViewElementNumber, isViewElementBoolean, isViewElementObjectOrList, isViewElementSelection } from "../models/uiModels";

import Fab from '@material-ui/core/Fab';
import AddIcon from '@material-ui/icons/Add';
import RemoveIcon from '@material-ui/icons/RemoveCircleOutline';
import SaveIcon from '@material-ui/icons/Save';
import EditIcon from '@material-ui/icons/Edit';
import Tooltip from "@material-ui/core/Tooltip";
import TextField from "@material-ui/core/TextField";
import FormControl from "@material-ui/core/FormControl";
import IconButton from "@material-ui/core/IconButton";
import Button from "@material-ui/core/Button";
import InputAdornment from "@material-ui/core/InputAdornment";
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import Link from "@material-ui/core/Link";
import FormHelperText from '@material-ui/core/FormHelperText';

const styles = (theme: Theme) => createStyles({
  header: {
    "display": "flex",
    "justifyContent": "space-between",
  },
  leftButton: {
    "justifyContent": "left"
  },
  outer: {
    "flex": "1",
    "heigh": "100%",
    "display": "flex",
    "alignItems": "center",
    "justifyContent": "center",
  },
  inner: {

  },
  "icon": {
    "marginRight": theme.spacing(0.5),
    "width": 20,
    "height": 20,
  },
  "fab": {
    "margin": theme.spacing(1),
  },
  button: {
    margin: 0,
    padding: "6px 6px",
    minWidth: 'unset'
  },
  readOnly: {
    '& label.Mui-focused': {
      color: 'green',
    },
    '& .MuiInput-underline:after': {
      borderBottomColor: 'green',
    },
    '& .MuiOutlinedInput-root': {
      '& fieldset': {
        borderColor: 'red',
      },
      '&:hover fieldset': {
        borderColor: 'yellow',
      },
      '&.Mui-focused fieldset': {
        borderColor: 'green',
      },
    },
  },
});

const mapProps = (state: IApplicationStoreState) => ({
  collectingData: state.configuration.valueSelector.collectingData,
  listKeyProperty: state.configuration.valueSelector.keyProperty,
  listSpecification: state.configuration.valueSelector.listSpecification,
  listData: state.configuration.valueSelector.listData,
  vPath: state.configuration.viewDescription.vPath,
  nodeId: state.configuration.deviceDescription.nodeId,
  viewData: state.configuration.viewDescription.viewData,
  viewSpecification: state.configuration.viewDescription.viewSpecification,
  displayAsList: state.configuration.viewDescription.displayAsList,
  keyProperty: state.configuration.viewDescription.keyProperty,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  onValueSelected: (value: any) => dispatcher.dispatch(new SetSelectedValue(value)),
  onUpdateData: (vPath: string, data: any) => dispatcher.dispatch(updateDataActionAsyncCreator(vPath, data)),
});

const SelectElementTable = MaterialTable as MaterialTableCtorType<{ [key: string]: any }>;

type ConfigurationApplicationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDispatch> & WithStyles<typeof styles>;

type ConfigurationApplicationComponentState = {
  isNew: boolean;
  editMode: boolean;
  canEdit: boolean;
  viewData: { [key: string]: any } | null;
}

const OldProps = Symbol("OldProps");
class ConfigurationApplicationComponent extends React.Component<ConfigurationApplicationComponentProps, ConfigurationApplicationComponentState> {

  /**
   *
   */
  constructor (props: ConfigurationApplicationComponentProps) {
    super(props);

    this.state = {
      isNew: false,
      canEdit: false,
      editMode: false,
      viewData: null
    }
  }

  static getDerivedStateFromProps(nextProps: ConfigurationApplicationComponentProps, prevState: ConfigurationApplicationComponentState & { [OldProps]: ConfigurationApplicationComponentProps  }) {

    if (!prevState || !prevState[OldProps] || (prevState[OldProps].viewData !== nextProps.viewData)) {
      const isNew: boolean = nextProps.vPath?.endsWith("[]") || false;
      const state = {
        ...prevState,
        isNew: isNew,
        editMode: isNew,
        viewData: nextProps.viewData || null,
        [OldProps]: nextProps,
      }
      return state;
    }
    return null;
  }

  private navigate = (path: string) => {
    this.props.history.push(`${this.props.match.url}${path}`);
  }

  private changeValueFor = (property: string, value: any) => {
    this.setState({
      viewData: {
        ...this.state.viewData,
        [property]: value
      }
    });
  }

  private renderUIElement = (viewSpecification: ViewSpecification, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const elements = viewSpecification.elements;
    return (
      Object.keys(elements).sort((a, b) => {
        const vsA = elements[a];
        const vsB = elements[b];
        if (keyProperty) {
          // if (vsA.label === vsB.label) return 0;
          if (vsA.label === keyProperty) return -1;
          if (vsB.label === keyProperty) return +1;
        }

        if (vsA.uiType === vsB.uiType) return 0;
        if (vsA.uiType !== "object" && vsB.uiType !== "object") return 0;
        if (vsA.uiType === "object") return +1;
        return -1;
      }).map(key => {
        const uiElement = elements[key];
        const isKey = (uiElement.label === keyProperty);
        const canEdit = editMode && (isNew || (uiElement.config && !isKey));
        if (isViewElementSelection(uiElement)) {
          let error = ""
          const value = String(viewData[uiElement.id]).toLowerCase();
          if (uiElement.mandatory && !!value) {
            error = "Error";
          }
          return (canEdit || viewData[uiElement.id] != null
            ? (<FormControl key={uiElement.id} style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
              <InputLabel htmlFor={`select-${uiElement.id}`} >{uiElement.label}</InputLabel>
              <Select
                required={!!uiElement.mandatory}
                error={!!error}
                onChange={(e) => { this.changeValueFor(uiElement.id, e.target.value) }}
                readOnly={!canEdit}
                disabled={editMode && !canEdit}
                value={(viewData[uiElement.id] || '').toString().toLowerCase()}
                inputProps={{
                  name: uiElement.id,
                  id: `select-${uiElement.id}`,
                }}
              >
                {uiElement.options.map(option => (<MenuItem key={option.key} title={option.description} value={option.value}>{option.key}</MenuItem>))}
              </Select>
              <FormHelperText>{error}</FormHelperText>
            </FormControl>)
            : null
          );
        } else if (isViewElementBoolean(uiElement)) {
          let error = ""
          const value = String(viewData[uiElement.id]).toLowerCase();
          if (uiElement.mandatory && value !== "true" && value !== "false") {
            error = "Error";
          }
          return (canEdit || viewData[uiElement.id] != null
            ? (<FormControl key={uiElement.id} style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
              <InputLabel htmlFor={`select-${uiElement.id}`} >{uiElement.label}</InputLabel>
              <Select
                required={!!uiElement.mandatory}
                error={!!error}
                onChange={(e) => { this.changeValueFor(uiElement.id, e.target.value) }}
                readOnly={!canEdit}
                disabled={editMode && !canEdit}
                value={value}
                inputProps={{
                  name: uiElement.id,
                  id: `select-${uiElement.id}`,
                }}
              >
                <MenuItem value={'true'}>{uiElement.trueValue || 'True'}</MenuItem>
                <MenuItem value={'false'}>{uiElement.falseValue || 'False'}</MenuItem>

              </Select>
              <FormHelperText>{error}</FormHelperText>
            </FormControl>)
            : null
          );
        } else if (isViewElementString(uiElement)) {
          return (
            <Tooltip key={uiElement.id} title={uiElement.description || ''}>
              <TextField InputProps={{ readOnly: !canEdit, disabled: editMode && !canEdit }} spellCheck={false} autoFocus margin="dense"
                id={uiElement.id} label={isKey ? "🔑 " + uiElement.label : uiElement.label} type="text" value={viewData[uiElement.id] || ''}
                style={{ width: 485, marginLeft: 20, marginRight: 20 }}
                onChange={(e) => { this.changeValueFor(uiElement.id, e.target.value) }}
              />
            </Tooltip>
          );
        } else if (isViewElementNumber(uiElement)) {
          return (
            <Tooltip key={uiElement.id} title={uiElement.description || ''}>
              <TextField InputProps={{ readOnly: !canEdit, disabled: editMode && !canEdit, startAdornment: uiElement.units != null ? <InputAdornment position="start">{uiElement.units}</InputAdornment> : undefined }} spellCheck={false} autoFocus margin="dense"
                id={uiElement.id} label={uiElement.label} type="text" value={viewData[uiElement.id] == null ? '' : viewData[uiElement.id]}
                style={{ width: 485, marginLeft: 20, marginRight: 20 }}
                onChange={(e) => { this.changeValueFor(uiElement.id, e.target.value) }}
              />
            </Tooltip>
          );
        } else if (isViewElementObjectOrList(uiElement)) {
          return (
            <FormControl key={uiElement.id} style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
              <Tooltip title={uiElement.description || ''}>
                <Button className={this.props.classes.leftButton} color="secondary" disabled={this.state.editMode} onClick={() => {
                  this.navigate(`/${uiElement.id}`);
                }}>{uiElement.label}</Button>
              </Tooltip>
            </FormControl>
          );
        } else {
          if (process.env.NODE_ENV !== "production") {
            console.error(`Unknown type - ${(uiElement as any).uiType} in ${(uiElement as any).id}.`)
          }
          return null;
        }
      })
    );
  };

  private renderUIElementList(listSpecification: ViewSpecification, listKeyProperty: string, listData: { [key: string]: any }[]) {
    const listElements = listSpecification.elements;

    const navigate = (path: string) => {
      this.props.history.push(`${this.props.match.url}${path}`);
    };

    const addNewElementAction = {
      icon: AddIcon, tooltip: 'Add', onClick: () => {
        navigate("[]"); // empty key means new element
      }
    };

    const { classes } = this.props;

    return (
      <SelectElementTable idProperty={listKeyProperty} rows={listData} customActionButtons={[addNewElementAction]} columns={
        Object.keys(listElements).reduce<ColumnModel<{ [key: string]: any }>[]>((acc, cur) => {
          const elm = listElements[cur];
          if (elm.uiType !== "object" && listData.every(entry => entry[elm.label] != null)) {
            if (elm.label !== listKeyProperty) {
              acc.push({ property: elm.label, type: elm.uiType === "number" ? ColumnType.numeric : ColumnType.text });
            } else {
              acc.unshift({ property: elm.label, type: elm.uiType === "number" ? ColumnType.numeric : ColumnType.text });
            }
          }
          return acc;
        }, []).concat([{
          property: "Actions", disableFilter: true, disableSorting: true, type: ColumnType.custom, customControl: (row => {
            return (
              <Tooltip title={"Remove"} >
                <IconButton className={classes.button} onClick={event => {

                }} >
                  <RemoveIcon />
                </IconButton>
              </Tooltip>
            )
          })
        }])
      } onHandleClick={(ev, row) => {
        ev.preventDefault();
        navigate(`[${row[listKeyProperty]}]`);
      }} ></SelectElementTable>
    );
  }

  private renderBreadCrumps() {
    const { editMode } = this.state;
    const { viewSpecification, displayAsList } = this.props;
    const { vPath, match: { url, path }, nodeId } = this.props;
    const pathParts = splitVPath(vPath!, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
    let lastPath = `/configuration`;
    let basePath = `/configuration/${nodeId}`;
    return (
      <div className={this.props.classes.header}>
        <div>
          <Breadcrumbs aria-label="breadcrumb">
            <Link color="inherit" href="#" onClick={(ev: React.MouseEvent<HTMLElement>) => {
              ev.preventDefault();
              this.props.history.push(lastPath);
            }}>Back</Link>
            <Link color="inherit" href="#" onClick={(ev: React.MouseEvent<HTMLElement>) => {
              ev.preventDefault();
              this.props.history.push(`/configuration/${nodeId}`);
            }}><span>{nodeId}</span></Link>
            {
              pathParts.map(([prop, key], ind) => {
                const path = `${basePath}/${prop}`;
                const keyPath = key && `${basePath}/${prop}[${key}]`;
                const ret = (
                  <span key={ind}>
                    <Link color="inherit" href="#" onClick={(ev: React.MouseEvent<HTMLElement>) => {
                      ev.preventDefault();
                      this.props.history.push(path);
                    }}><span>{prop.replace(/^[^:]+:/, "")}</span></Link>
                    {
                      keyPath && <Link color="inherit" href="#" onClick={(ev: React.MouseEvent<HTMLElement>) => {
                        ev.preventDefault();
                        this.props.history.push(keyPath);
                      }}>{`[${key}]`}</Link> || null
                    }
                  </span>
                );
                lastPath = basePath;
                basePath = keyPath || path;
                return ret;
              })
            }
          </Breadcrumbs>
        </div>
        { /* do not show edit if this is a list or it can't be edited */
          !displayAsList && viewSpecification.canEdit && (<div>
            <Fab color="secondary" aria-label="edit" className={this.props.classes.fab} onClick={() => {
              if (this.state.editMode) {
                this.props.onUpdateData(this.props.vPath!, this.state.viewData);
              }
              this.setState({ editMode: !editMode });
            }}>
              {editMode
                ? <SaveIcon />
                : <EditIcon />
              }
            </Fab>
          </div> || null)
        }
      </div>
    );
  }

  private renderValueSelector() {
    const { listKeyProperty, listSpecification, listData, onValueSelected } = this.props;
    if (!listKeyProperty || !listSpecification) {
      throw new Error("ListKex ot view not specified.");
    }

    return (
      <div>
        <SelectElementTable idProperty={listKeyProperty} rows={listData} columns={
          Object.keys(listSpecification.elements).reduce<ColumnModel<{ [key: string]: any }>[]>((acc, cur) => {
            const elm = listSpecification.elements[cur];
            if (elm.uiType !== "object" && listData.every(entry => entry[elm.label] != null)) {
              if (elm.label !== listKeyProperty) {
                acc.push({ property: elm.label, type: elm.uiType === "number" ? ColumnType.numeric : ColumnType.text });
              } else {
                acc.unshift({ property: elm.label, type: elm.uiType === "number" ? ColumnType.numeric : ColumnType.text });
              }
            }
            return acc;
          }, [])
        } onHandleClick={(ev, row) => { ev.preventDefault(); onValueSelected(row); }} ></SelectElementTable>
      </div>
    );
  }

  private renderValueEditor() {
    const { keyProperty, displayAsList, viewSpecification } = this.props;
    const { viewData, editMode, isNew } = this.state;

    return (
      <div>
        { this.renderBreadCrumps() }
        { displayAsList && viewData instanceof Array
            ? this.renderUIElementList(viewSpecification, keyProperty!, viewData)
            : this.renderUIElement(viewSpecification, viewData!, keyProperty, editMode, isNew)
        }
      </div >
    );
  }

  private renderCollectingData() {
    return (
      <div className={this.props.classes.outer}>
        <div className={this.props.classes.inner}>
          <Loader />
          <h3>Collecting Data ...</h3>
        </div>
      </div>
    );
  }

  render() {
    return this.props.collectingData || !this.state.viewData
      ? this.renderCollectingData()
      : this.props.listSpecification
        ? this.renderValueSelector()
        : this.renderValueEditor();
  }
}

export const ConfigurationApplication = withStyles(styles)(withRouter(connect(mapProps, mapDispatch)(ConfigurationApplicationComponent)));
export default ConfigurationApplication;
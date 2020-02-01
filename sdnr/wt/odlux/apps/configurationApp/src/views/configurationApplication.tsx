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

import { WithStyles, withStyles, createStyles, Theme } from '@material-ui/core/styles';

import connect, { IDispatcher, Connect } from "../../../../framework/src/flux/connect";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import MaterialTable, { ColumnModel, ColumnType, MaterialTableCtorType } from "../../../../framework/src/components/material-table";
import { Loader } from "../../../../framework/src/components/material-ui/loader";

import { SetSelectedValue, splitVPath, updateDataActionAsyncCreator, updateViewActionAsyncCreator } from "../actions/deviceActions";
import { ViewSpecification, isViewElementString, isViewElementNumber, isViewElementBoolean, isViewElementObjectOrList, isViewElementSelection, isViewElementChoise, ViewElement, ViewElementChoise, isViewElementUnion } from "../models/uiModels";

import Fab from '@material-ui/core/Fab';
import AddIcon from '@material-ui/icons/Add';
import ArrowBack from '@material-ui/icons/ArrowBack';
import RemoveIcon from '@material-ui/icons/RemoveCircleOutline';
import SaveIcon from '@material-ui/icons/Save';
import EditIcon from '@material-ui/icons/Edit';
import Tooltip from "@material-ui/core/Tooltip";
import TextField from "@material-ui/core/TextField";
import FormControl from "@material-ui/core/FormControl";
import IconButton from "@material-ui/core/IconButton";

import InputAdornment from "@material-ui/core/InputAdornment";
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import Link from "@material-ui/core/Link";
import FormHelperText from '@material-ui/core/FormHelperText';

import { UIElementReference } from '../components/uiElementReference';
import { UiElementNumber } from '../components/uiElementNumber';
import { UiElementString } from '../components/uiElementString';
import { UiElementBoolean } from '../components/uiElementBoolean';
import { UiElementSelection } from '../components/uiElementSelection';
import { UIElementUnion } from '../components/uiElementUnion';

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
  section: {
    padding: "15px",
    borderBottom: `2px solid ${theme.palette.divider}`,
  },
  viewElements: {
    width: 485, marginLeft: 20, marginRight: 20
  },
  verificationElements: {
    width: 485, marginLeft: 20, marginRight: 20
  }
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
  reloadView: (vPath: string) => dispatcher.dispatch(updateViewActionAsyncCreator(vPath)),
});

const SelectElementTable = MaterialTable as MaterialTableCtorType<{ [key: string]: any }>;

type ConfigurationApplicationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDispatch> & WithStyles<typeof styles>;

type ConfigurationApplicationComponentState = {
  isNew: boolean;
  editMode: boolean;
  canEdit: boolean;
  viewData: { [key: string]: any } | null;
  choises: { [path: string]: { selectedCase: string, data: { [property: string]: any } } };
}

const OldProps = Symbol("OldProps");
class ConfigurationApplicationComponent extends React.Component<ConfigurationApplicationComponentProps, ConfigurationApplicationComponentState> {

  /**
   *
   */
  constructor(props: ConfigurationApplicationComponentProps) {
    super(props);

    this.state = {
      isNew: false,
      canEdit: false,
      editMode: false,
      viewData: null,
      choises: {},
    }
  }

  static getDerivedStateFromProps(nextProps: ConfigurationApplicationComponentProps, prevState: ConfigurationApplicationComponentState & { [OldProps]: ConfigurationApplicationComponentProps }) {

    if (!prevState || !prevState[OldProps] || (prevState[OldProps].viewData !== nextProps.viewData)) {
      const isNew: boolean = nextProps.vPath ?.endsWith("[]") || false;
      const state = {
        ...prevState,
        isNew: isNew,
        editMode: isNew,
        viewData: nextProps.viewData || null,
        [OldProps]: nextProps,
        choises: nextProps.viewSpecification && Object.keys(nextProps.viewSpecification.elements).reduce((acc, cur) => {
          const elm = nextProps.viewSpecification.elements[cur];
          if (isViewElementChoise(elm)) {
            const caseKeys = Object.keys(elm.cases);

            // find the right case for this choise, use the first one with data, at least use index 0
            const selectedCase = caseKeys.find(key => {
              const caseElm = elm.cases[key];
              return Object.keys(caseElm.elements).some(caseElmKey => {
                const caseElmElm = caseElm.elements[caseElmKey];
                return nextProps.viewData[caseElmElm.label] != null || nextProps.viewData[caseElmElm.id] != null;
              });
            }) || caseKeys[0];

            // extract all data of the active case
            const caseElements = elm.cases[selectedCase].elements;
            const data = Object.keys(caseElements).reduce((dataAcc, dataCur) => {
              const dataElm = caseElements[dataCur];
              if (nextProps.viewData[dataElm.label] !== undefined) {
                dataAcc[dataElm.label] = nextProps.viewData[dataElm.label];
              } else if (nextProps.viewData[dataElm.id] !== undefined) {
                dataAcc[dataElm.id] = nextProps.viewData[dataElm.id];
              }
              return dataAcc;
            }, {} as { [name: string]: any });

            acc[elm.id] = {
              selectedCase,
              data,
            };
          }
          return acc;
        }, {} as { [path: string]: { selectedCase: string, data: { [property: string]: any } } }) || {}
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

  private renderUIElement = (uiElement: ViewElement, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const isKey = (uiElement.label === keyProperty);
    const canEdit = editMode && (isNew || (uiElement.config && !isKey));
    if (isViewElementSelection(uiElement)) {

      return <UiElementSelection
        key={uiElement.id}
        inputValue={viewData[uiElement.id] || ''}
        value={uiElement}
        readOnly={!canEdit}
        disabled={editMode && !canEdit}
        onChange={(e) => { this.changeValueFor(uiElement.id, e) }}
      />

    } else if (isViewElementBoolean(uiElement)) {
      return <UiElementBoolean
        key={uiElement.id}
        inputValue={viewData[uiElement.id] || ''}
        value={uiElement}
        readOnly={!canEdit}
        disabled={editMode && !canEdit}
        onChange={(e) => { this.changeValueFor(uiElement.id, e) }} />

    } else if (isViewElementString(uiElement)) {
      return <UiElementString
        key={uiElement.id}
        inputValue={viewData[uiElement.id] || ''}
        value={uiElement}
        isKey={isKey}
        readOnly={!canEdit}
        disabled={editMode && !canEdit}
        onChange={(e) => { this.changeValueFor(uiElement.id, e) }} />

    } else if (isViewElementNumber(uiElement)) {
      return <UiElementNumber
        key={uiElement.id}
        value={uiElement}
        inputValue={viewData[uiElement.id] == null ? '' : viewData[uiElement.id]}
        readOnly={!canEdit}
        disabled={editMode && !canEdit}
        onChange={(e) => { this.changeValueFor(uiElement.id, e) }} />
    } else if (isViewElementUnion(uiElement)) {
      return <UIElementUnion
        key={uiElement.id}
        isKey={false}
        value={uiElement}
        inputValue={viewData[uiElement.id] == null ? '' : viewData[uiElement.id]}
        readOnly={!canEdit}
        disabled={editMode && !canEdit}
        onChange={(e) => { this.changeValueFor(uiElement.id, e) }} />
    }
    else {
      if (process.env.NODE_ENV !== "production") {
        console.error(`Unknown element type - ${(uiElement as any).uiType} in ${(uiElement as any).id}.`)
      }
      return null;
    }
  };

  // private renderUIReference = (uiElement: ViewElement, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
  //   const isKey = (uiElement.label === keyProperty);
  //   const canEdit = editMode && (isNew || (uiElement.config && !isKey));
  //   if (isViewElementObjectOrList(uiElement)) {
  //     return (
  //       <FormControl key={uiElement.id} style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
  //         <Tooltip title={uiElement.description || ''}>
  //           <Button className={this.props.classes.leftButton} color="secondary" disabled={this.state.editMode} onClick={() => {
  //             this.navigate(`/${uiElement.id}`);
  //           }}>{uiElement.label}</Button>
  //         </Tooltip>
  //       </FormControl>
  //     );
  //   } else {
  //     if (process.env.NODE_ENV !== "production") {
  //       console.error(`Unknown reference type - ${(uiElement as any).uiType} in ${(uiElement as any).id}.`)
  //     }
  //     return null;
  //   }
  // };

  private renderUIChoise = (uiElement: ViewElementChoise, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const isKey = (uiElement.label === keyProperty);

    const currentChoise = this.state.choises[uiElement.id];
    const currentCase = currentChoise && uiElement.cases[currentChoise.selectedCase];

    const canEdit = editMode && (isNew || (uiElement.config && !isKey));
    if (isViewElementChoise(uiElement)) {
      const subElements = currentCase ?.elements;
      return (
        <>
          <FormControl key={uiElement.id} style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
            <InputLabel htmlFor={`select-${uiElement.id}`} >{uiElement.label}</InputLabel>
            <Select
              required={!!uiElement.mandatory}
              onChange={(e) => {
                if (currentChoise.selectedCase === e.target.value) {
                  return; // nothing changed
                }
                this.setState({ choises: { ...this.state.choises, [uiElement.id]: { ...this.state.choises[uiElement.id], selectedCase: e.target.value as string } } });
              }}
              readOnly={!canEdit}
              disabled={editMode && !canEdit}
              value={this.state.choises[uiElement.id].selectedCase}
              inputProps={{
                name: uiElement.id,
                id: `select-${uiElement.id}`,
              }}
            >
              {
                Object.keys(uiElement.cases).map(caseKey => {
                  const caseElm = uiElement.cases[caseKey];
                  return (
                    <MenuItem key={caseElm.id} title={caseElm.description} value={caseKey}>{caseElm.label}</MenuItem>
                  );
                })
              }
            </Select>
          </FormControl>
          {subElements
            ? Object.keys(subElements).map(elmKey => {
              const elm = subElements[elmKey];
              return this.renderUIElement(elm, viewData, keyProperty, editMode, isNew);
            })
            : <h3>Invalid Choise</h3>
          }
        </>
      );
    } else {
      if (process.env.NODE_ENV !== "production") {
        console.error(`Unknown type - ${(uiElement as any).uiType} in ${(uiElement as any).id}.`)
      }
      return null;
    }
  };

  private renderUIView = (viewSpecification: ViewSpecification, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const { classes } = this.props;

    const orderFunc = (vsA: ViewElement, vsB: ViewElement) => {
      if (keyProperty) {
        // if (vsA.label === vsB.label) return 0;
        if (vsA.label === keyProperty) return -1;
        if (vsB.label === keyProperty) return +1;
      }

      // if (vsA.uiType === vsB.uiType) return 0;
      // if (vsA.uiType !== "object" && vsB.uiType !== "object") return 0;
      // if (vsA.uiType === "object") return +1;
      return -1;
    };

    const sections = Object.keys(viewSpecification.elements).reduce((acc, cur) => {
      const elm = viewSpecification.elements[cur];
      if (isViewElementObjectOrList(elm)) {
        acc.references.push(elm);
      } else if (isViewElementChoise(elm)) {
        acc.choises.push(elm);
      } else {
        acc.elements.push(elm);
      }
      return acc;
    }, { elements: [] as ViewElement[], references: [] as ViewElement[], choises: [] as ViewElementChoise[] });

    sections.elements = sections.elements.sort(orderFunc);

    return (
      <>
        <div className={classes.section} />
        {sections.elements.length > 0
          ? (
            <div className={classes.section}>
              {sections.elements.map(element => this.renderUIElement(element, viewData, keyProperty, editMode, isNew))}
            </div>
          ) : null
        }
        {sections.references.length > 0
          ? (
            <div className={classes.section}>
              {sections.references.map(element => (
                <UIElementReference key={element.id} element={element} disabled={editMode} onOpenReference={(elm) => { this.navigate(`/${elm.id}`) }} />
              ))}
            </div>
          ) : null
        }
        {sections.choises.length > 0
          ? (
            <div className={classes.section}>
              {sections.choises.map(element => this.renderUIChoise(element, viewData, keyProperty, editMode, isNew))}
            </div>
          ) : null
        }
      </>
    );
  };

  private renderUIViewList(listSpecification: ViewSpecification, listKeyProperty: string, listData: { [key: string]: any }[]) {
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
        {this.state.editMode && (
          <Fab color="secondary" aria-label="edit" className={this.props.classes.fab} onClick={async () => {
            this.props.vPath && await this.props.reloadView(this.props.vPath);
            this.setState({ editMode: false });
          }} ><ArrowBack /></Fab>
        ) || null}
        { /* do not show edit if this is a list or it can't be edited */
          !displayAsList && viewSpecification.canEdit && (<div>
            <Fab color="secondary" aria-label="edit" className={this.props.classes.fab} onClick={() => {
              if (this.state.editMode) {

                // ensure only active choises will be contained
                const choiseKeys = Object.keys(viewSpecification.elements).filter(elmKey => isViewElementChoise(viewSpecification.elements[elmKey]));
                const elementsToRemove = choiseKeys.reduce((acc, cur) => {
                  const choise = viewSpecification.elements[cur] as ViewElementChoise;
                  const selectedCase = this.state.choises[cur].selectedCase;
                  Object.keys(choise.cases).forEach(caseKey => {
                    if (caseKey === selectedCase) return;
                    const caseElements = choise.cases[caseKey].elements;
                    Object.keys(caseElements).forEach(caseElementKey => {
                      acc.push(caseElements[caseElementKey]);
                    });
                  });
                  return acc;
                }, [] as ViewElement[]);

                const viewData = this.state.viewData;
                const resultingViewData = viewData && Object.keys(viewData).reduce((acc, cur) => {
                  if (!elementsToRemove.some(elm => elm.label === cur || elm.id === cur)) {
                    acc[cur] = viewData[cur];
                  }
                  return acc;
                }, {} as { [key: string]: any });

                this.props.onUpdateData(this.props.vPath!, resultingViewData);
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
        {this.renderBreadCrumps()}
        {displayAsList && viewData instanceof Array
          ? this.renderUIViewList(viewSpecification, keyProperty!, viewData)
          : this.renderUIView(viewSpecification, viewData!, keyProperty, editMode, isNew)
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
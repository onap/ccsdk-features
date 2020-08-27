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
import { renderObject } from '../../../../framework/src/components/objectDump';

import { DisplayModeType } from '../handlers/viewDescriptionHandler';
import { SetSelectedValue, splitVPath, updateDataActionAsyncCreator, updateViewActionAsyncCreator, removeElementActionAsyncCreator, executeRpcActionAsyncCreator } from "../actions/deviceActions";
import { ViewSpecification, isViewElementString, isViewElementNumber, isViewElementBoolean, isViewElementObjectOrList, isViewElementSelection, isViewElementChoise, ViewElement, ViewElementChoise, isViewElementUnion, isViewElementRpc, ViewElementRpc, isViewElementEmpty } from "../models/uiModels";

import Fab from '@material-ui/core/Fab';
import AddIcon from '@material-ui/icons/Add';
import ArrowBack from '@material-ui/icons/ArrowBack';
import RemoveIcon from '@material-ui/icons/RemoveCircleOutline';
import SaveIcon from '@material-ui/icons/Save';
import EditIcon from '@material-ui/icons/Edit';
import Tooltip from "@material-ui/core/Tooltip";
import FormControl from "@material-ui/core/FormControl";
import IconButton from "@material-ui/core/IconButton";

import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import Link from "@material-ui/core/Link";

import { UIElementReference } from '../components/uiElementReference';
import { UiElementNumber } from '../components/uiElementNumber';
import { UiElementString } from '../components/uiElementString';
import { UiElementBoolean } from '../components/uiElementBoolean';
import { UiElementSelection } from '../components/uiElementSelection';
import { UIElementUnion } from '../components/uiElementUnion';
import { Button } from '@material-ui/core';

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
    "height": "100%",
    "display": "flex",
    "alignItems": "center",
    "justifyContent": "center",
  },
  inner: {

  },
  container: {
    "height": "100%",
    "display": "flex",
    "flexDirection": "column",
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
  uiView: {
    overflowY: "auto",
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
  outputData: state.configuration.viewDescription.outputData,
  displaySpecification: state.configuration.viewDescription.displaySpecification,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  onValueSelected: (value: any) => dispatcher.dispatch(new SetSelectedValue(value)),
  onUpdateData: (vPath: string, data: any) => dispatcher.dispatch(updateDataActionAsyncCreator(vPath, data)),
  reloadView: (vPath: string) => dispatcher.dispatch(updateViewActionAsyncCreator(vPath)),
  removeElement: (vPath: string) => dispatcher.dispatch(removeElementActionAsyncCreator(vPath)),
  executeRpc: (vPath: string, data: any) => dispatcher.dispatch(executeRpcActionAsyncCreator(vPath, data)),
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

  private static getChoisesFromElements = (elements: { [name: string]: ViewElement }, viewData: any) => {
    return Object.keys(elements).reduce((acc, cur) => {
      const elm = elements[cur];
      if (isViewElementChoise(elm)) {
        const caseKeys = Object.keys(elm.cases);

        // find the right case for this choise, use the first one with data, at least use index 0
        const selectedCase = caseKeys.find(key => {
          const caseElm = elm.cases[key];
          return Object.keys(caseElm.elements).some(caseElmKey => {
            const caseElmElm = caseElm.elements[caseElmKey];
            return viewData[caseElmElm.label] !== undefined || viewData[caseElmElm.id] != undefined;
          });
        }) || caseKeys[0];

        // extract all data of the active case
        const caseElements = elm.cases[selectedCase].elements;
        const data = Object.keys(caseElements).reduce((dataAcc, dataCur) => {
          const dataElm = caseElements[dataCur];
          if (isViewElementEmpty(dataElm)) {
            dataAcc[dataElm.label] = null;
          } else if (viewData[dataElm.label] !== undefined) {
            dataAcc[dataElm.label] = viewData[dataElm.label];
          } else if (viewData[dataElm.id] !== undefined) {
            dataAcc[dataElm.id] = viewData[dataElm.id];
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

  static getDerivedStateFromProps(nextProps: ConfigurationApplicationComponentProps, prevState: ConfigurationApplicationComponentState & { [OldProps]: ConfigurationApplicationComponentProps }) {

    if (!prevState || !prevState[OldProps] || (prevState[OldProps].viewData !== nextProps.viewData)) {
      const isNew: boolean = nextProps.vPath?.endsWith("[]") || false;
      const state = {
        ...prevState,
        isNew: isNew,
        editMode: isNew,
        viewData: nextProps.viewData || null,
        [OldProps]: nextProps,
        choises: nextProps.displaySpecification.displayMode === DisplayModeType.doNotDisplay
          ? null
          : nextProps.displaySpecification.displayMode === DisplayModeType.displayAsRPC
            ? nextProps.displaySpecification.inputViewSpecification && ConfigurationApplicationComponent.getChoisesFromElements(nextProps.displaySpecification.inputViewSpecification.elements, nextProps.viewData) || []
            : ConfigurationApplicationComponent.getChoisesFromElements(nextProps.displaySpecification.viewSpecification.elements, nextProps.viewData)
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

  private collectData = (elements: { [name: string]: ViewElement }) => {
    // ensure only active choises will be contained
    const viewData : { [key: string]: any }= { ...this.state.viewData };
    const choiseKeys = Object.keys(elements).filter(elmKey => isViewElementChoise(elements[elmKey]));
    const elementsToRemove = choiseKeys.reduce((acc, curChoiceKey) => {
      const currentChoice = elements[curChoiceKey] as ViewElementChoise;
      const selectedCase = this.state.choises[curChoiceKey].selectedCase;
      Object.keys(currentChoice.cases).forEach(caseKey => {
        const caseElements = currentChoice.cases[caseKey].elements;
        if (caseKey === selectedCase) {
          Object.keys(caseElements).forEach(caseElementKey => {
            const elm = caseElements[caseElementKey];
            if (isViewElementEmpty(elm)) {
              // insert null for all empty elements
              viewData[elm.id] = null;
            }
          });
          return;
        };
        Object.keys(caseElements).forEach(caseElementKey => {
          acc.push(caseElements[caseElementKey]);
        });
      });
      return acc;
    }, [] as ViewElement[]);

    return viewData && Object.keys(viewData).reduce((acc, cur) => {
      if (!elementsToRemove.some(elm => elm.label === cur || elm.id === cur)) {
        acc[cur] = viewData[cur];
      }
      return acc;
    }, {} as { [key: string]: any });
  }

  private renderUIElement = (uiElement: ViewElement, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const isKey = (uiElement.label === keyProperty);
    const canEdit = editMode && (isNew || (uiElement.config && !isKey));
    
    // do not show elements w/o any value from the backend
    if (viewData[uiElement.id] == null && !editMode) {
      return null;
    } else  if (isViewElementEmpty(uiElement)) {
      return null;  
    } else if (isViewElementSelection(uiElement)) {

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
        inputValue={viewData[uiElement.id] == null ? '' : viewData[uiElement.id]}
        value={uiElement}
        readOnly={!canEdit}
        disabled={editMode && !canEdit}
        onChange={(e) => { this.changeValueFor(uiElement.id, e) }} />

    } else if (isViewElementString(uiElement)) {
      return <UiElementString
        key={uiElement.id}
        inputValue={viewData[uiElement.id] == null ? '' : viewData[uiElement.id]}
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
    } else {
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
      } else if (isViewElementRpc(elm)) {
        acc.rpcs.push(elm);
      } else {
        acc.elements.push(elm);
      }
      return acc;
    }, { elements: [] as ViewElement[], references: [] as ViewElement[], choises: [] as ViewElementChoise[], rpcs: [] as ViewElementRpc[] });

    sections.elements = sections.elements.sort(orderFunc);

    return (
      <div className={classes.uiView}>
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
        {sections.rpcs.length > 0
          ? (
            <div className={classes.section}>
              {sections.rpcs.map(element => (
                <UIElementReference key={element.id} element={element} disabled={editMode} onOpenReference={(elm) => { this.navigate(`/${elm.id}`) }} />
              ))}
            </div>
          ) : null
        }
      </div>
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

    const { classes, removeElement } = this.props;

    return (
      <SelectElementTable stickyHeader idProperty={listKeyProperty} rows={listData} customActionButtons={[addNewElementAction]} columns={
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
          property: "Actions", disableFilter: true, disableSorting: true, type: ColumnType.custom, customControl: ( ({ rowData })=> {
            return (
              <Tooltip title={"Remove"} >
                <IconButton className={classes.button} onClick={(e) => {
                  e.stopPropagation();
                  e.preventDefault();
                  removeElement(`${this.props.vPath}[${rowData[listKeyProperty]}]`)
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

  private renderUIViewRPC(inputViewSpecification: ViewSpecification | undefined, inputViewData: { [key: string]: any }, outputViewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) {
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

    const sections = inputViewSpecification && Object.keys(inputViewSpecification.elements).reduce((acc, cur) => {
      const elm = inputViewSpecification.elements[cur];
      if (isViewElementObjectOrList(elm)) {
        console.error("Object should not appear in RPC view !");
      } else if (isViewElementChoise(elm)) {
        acc.choises.push(elm);
      } else if (isViewElementRpc(elm)) {
        console.error("RPC should not appear in RPC view !");
      } else {
        acc.elements.push(elm);
      }
      return acc;
    }, { elements: [] as ViewElement[], references: [] as ViewElement[], choises: [] as ViewElementChoise[], rpcs: [] as ViewElementRpc[] })
      || { elements: [] as ViewElement[], references: [] as ViewElement[], choises: [] as ViewElementChoise[], rpcs: [] as ViewElementRpc[] };

    sections.elements = sections.elements.sort(orderFunc);

    return (
      <>
        <div className={classes.section} />
        {sections.elements.length > 0
          ? (
            <div className={classes.section}>
              {sections.elements.map(element => this.renderUIElement(element, inputViewData, keyProperty, editMode, isNew))}
            </div>
          ) : null
        }
        {sections.choises.length > 0
          ? (
            <div className={classes.section}>
              {sections.choises.map(element => this.renderUIChoise(element, inputViewData, keyProperty, editMode, isNew))}
            </div>
          ) : null
        }
        <Button onClick={() => {
          const resultingViewData = inputViewSpecification && this.collectData(inputViewSpecification.elements);
          this.props.executeRpc(this.props.vPath!, resultingViewData);
        }} >Exec</Button>
        {outputViewData !== undefined
          ? (
            renderObject(outputViewData)          )
          : null
        }
        </>
    );
  };

  private renderBreadCrumps() {
    const { editMode } = this.state;
    const { displaySpecification } = this.props;
    const { vPath, nodeId } = this.props;
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
          displaySpecification.displayMode === DisplayModeType.displayAsObject && displaySpecification.viewSpecification.canEdit && (<div>
            <Fab color="secondary" aria-label="edit" className={this.props.classes.fab} onClick={() => {
              if (this.state.editMode) {
                // ensure only active choises will be contained
                const resultingViewData = this.collectData(displaySpecification.viewSpecification.elements);
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
      <div className={this.props.classes.container}>
        <SelectElementTable stickyHeader  idProperty={listKeyProperty} rows={listData} columns={
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
    const { displaySpecification: ds, outputData } = this.props;
    const { viewData, editMode, isNew } = this.state;

    return (
      <div className={this.props.classes.container}>
        {this.renderBreadCrumps()}
        {ds.displayMode === DisplayModeType.doNotDisplay
          ? null
          : ds.displayMode === DisplayModeType.displayAsList && viewData instanceof Array
            ? this.renderUIViewList(ds.viewSpecification, ds.keyProperty!, viewData)
            : ds.displayMode === DisplayModeType.displayAsRPC
              ? this.renderUIViewRPC(ds.inputViewSpecification, viewData!, outputData, undefined, true, false)
              : this.renderUIView(ds.viewSpecification, viewData!, ds.keyProperty, editMode, isNew)
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
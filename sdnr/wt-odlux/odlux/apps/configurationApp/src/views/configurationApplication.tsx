<<<<<<< HEAD   (907af9 fix oauth code)
=======
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
import { RouteComponentProps, withRouter } from 'react-router-dom';

import { Theme } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

import { useConfirm } from 'material-ui-confirm';

import { connect, IDispatcher, Connect } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import MaterialTable, { ColumnModel, ColumnType, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { Loader } from '../../../../framework/src/components/material-ui/loader';
import { renderObject } from '../../../../framework/src/components/objectDump';

import { DisplayModeType } from '../handlers/viewDescriptionHandler';
import {
  SetSelectedValue,
  updateDataActionAsyncCreator,
  updateViewActionAsyncCreator,
  removeElementActionAsyncCreator,
  executeRpcActionAsyncCreator,
} from '../actions/deviceActions';

import {
  ViewElement,
  ViewSpecification,
  ViewElementChoice,
  ViewElementRpc,
  isViewElementString,
  isViewElementNumber,
  isViewElementBoolean,
  isViewElementObjectOrList,
  isViewElementSelection,
  isViewElementChoice,
  isViewElementUnion,
  isViewElementRpc,
  isViewElementEmpty,
  isViewElementDate,
} from '../models/uiModels';

import { getAccessPolicyByUrl } from '../../../../framework/src/services/restService';

import Fab from '@mui/material/Fab';
import AddIcon from '@mui/icons-material/Add';
import PostAdd from '@mui/icons-material/PostAdd';
import ArrowBack from '@mui/icons-material/ArrowBack';
import RemoveIcon from '@mui/icons-material/RemoveCircleOutline';
import CheckIcon from '@mui/icons-material/Check';
import SaveIcon from '@mui/icons-material/Save';
import EditIcon from '@mui/icons-material/Edit';
import Tooltip from '@mui/material/Tooltip';
import FormControl from '@mui/material/FormControl';
import IconButton from '@mui/material/IconButton';

import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import Breadcrumbs from '@mui/material/Breadcrumbs';
import Button from '@mui/material/Button';
import Link from '@mui/material/Link';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

import { BaseProps } from '../components/baseProps';
import { UIElementReference } from '../components/uiElementReference';
import { UiElementNumber } from '../components/uiElementNumber';
import { UiElementString } from '../components/uiElementString';
import { UiElementBoolean } from '../components/uiElementBoolean';
import { UiElementSelection } from '../components/uiElementSelection';
import { UIElementUnion } from '../components/uiElementUnion';
import { UiElementLeafList } from '../components/uiElementLeafList';

import { splitVPath } from '../utilities/viewEngineHelper';

const styles = (theme: Theme) => createStyles({
  header: {
    'display': 'flex',
    'justifyContent': 'space-between',
  },
  leftButton: {
    'justifyContent': 'left',
  },
  outer: {
    'flex': '1',
    'height': '100%',
    'display': 'flex',
    'alignItems': 'center',
    'justifyContent': 'center',
  },
  inner: {

  },
  container: {
    'height': '100%',
    'display': 'flex',
    'flexDirection': 'column',
  },
  'icon': {
    'marginRight': theme.spacing(0.5),
    'width': 20,
    'height': 20,
  },
  'fab': {
    'margin': theme.spacing(1),
  },
  button: {
    margin: 0,
    padding: '6px 6px',
    minWidth: 'unset',
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
    overflowY: 'auto',
  },
  section: {
    padding: '15px',
    borderBottom: `2px solid ${theme.palette.divider}`,
  },
  viewElements: {
    width: 485, marginLeft: 20, marginRight: 20,
  },
  verificationElements: {
    width: 485, marginLeft: 20, marginRight: 20,
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular,
  },
  moduleCollection: {
    marginTop: '16px',
    overflow: 'auto',
  },
  objectReult: {
    overflow: 'auto',
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
  isNewSubElement: boolean;
  editMode: boolean;
  canEdit: boolean;
  viewData: { [key: string]: any } | null;
  choices: { [path: string]: { selectedCase: string; data: { [property: string]: any } } };
};

type GetStatelessComponentProps<T> = T extends (props: infer P & { children?: React.ReactNode }) => any ? P : any;
const AccordionSummaryExt: React.FC<GetStatelessComponentProps<typeof AccordionSummary>> = (props) => {
  const [disabled, setDisabled] = useState(true);
  const onMouseDown = (ev: React.MouseEvent<HTMLElement>) => {
    if (ev.button === 1) {
      setDisabled(!disabled);
      ev.preventDefault();
    }
  };
  return (
    <div onMouseDown={onMouseDown} >
      <AccordionSummary {...{ ...props, disabled: props.disabled && disabled }} />
    </div>
  );
};

const OldProps = Symbol('OldProps');
class ConfigurationApplicationComponent extends React.Component<ConfigurationApplicationComponentProps, ConfigurationApplicationComponentState> {

  /**
   *
   */
  constructor(props: ConfigurationApplicationComponentProps) {
    super(props);

    this.state = {
      isNew: false,
      isNewSubElement: false,
      canEdit: false,
      editMode: false,
      viewData: null,
      choices: {},
    };
  }

  private static getChoicesFromElements = (elements: { [name: string]: ViewElement }, viewData: any) => {
    return Object.keys(elements).reduce((acc, cur) => {
      const elm = elements[cur];
      if (isViewElementChoice(elm)) {
        const caseKeys = Object.keys(elm.cases);

        // find the right case for this choice, use the first one with data, at least use index 0
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
    }, {} as { [path: string]: { selectedCase: string; data: { [property: string]: any } } }) || {};
  };

  static getDerivedStateFromProps(nextProps: ConfigurationApplicationComponentProps, prevState: ConfigurationApplicationComponentState & { [OldProps]: ConfigurationApplicationComponentProps }) {

    if (!prevState || !prevState[OldProps] || (prevState[OldProps].viewData !== nextProps.viewData)) {
      const isNew: boolean = nextProps.vPath?.includes('[]') || false;
      const isNewSubElement: boolean = nextProps.vPath?.includes('[]') && !nextProps.vPath?.endsWith('[]') || false;

      const state = {
        ...prevState,
        isNew: isNew,
        editMode: isNew,
        isNewSubElement: isNewSubElement,
        viewData: nextProps.viewData || null,
        [OldProps]: nextProps,
        choices: nextProps.displaySpecification.displayMode === DisplayModeType.doNotDisplay
          || nextProps.displaySpecification.displayMode === DisplayModeType.displayAsMessage
          ? null
          : nextProps.displaySpecification.displayMode === DisplayModeType.displayAsRPC
            ? nextProps.displaySpecification.inputViewSpecification && ConfigurationApplicationComponent.getChoicesFromElements(nextProps.displaySpecification.inputViewSpecification.elements, nextProps.viewData) || []
            : ConfigurationApplicationComponent.getChoicesFromElements(nextProps.displaySpecification.viewSpecification.elements, nextProps.viewData),
      };
      return state;
    }
    return null;
  }

  private navigate = (path: string) => {
    this.props.history.push(`${this.props.match.url}${path}`);
  };

  private changeValueFor = (property: string, value: any) => {
    this.setState({
      viewData: {
        ...this.state.viewData,
        [property]: value,
      },
    });
  };

  private collectData = (elements: { [name: string]: ViewElement }) => {
    // ensure only active choices will be contained
    const viewData: { [key: string]: any } = { ...this.state.viewData };
    const choiceKeys = Object.keys(elements).filter(elmKey => isViewElementChoice(elements[elmKey]));
    const elementsToRemove = choiceKeys.reduce((acc, curChoiceKey) => {
      const currentChoice = elements[curChoiceKey] as ViewElementChoice;
      const selectedCase = this.state.choices[curChoiceKey].selectedCase;
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
        }
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
  };

  private isPolicyViewElementForbidden = (element: ViewElement, dataPath: string): boolean => {
    const policy = getAccessPolicyByUrl(`${dataPath}/${element.id}`);
    return !(policy.GET && policy.POST);
  };

  private isPolicyModuleForbidden = (moduleName: string, dataPath: string): boolean => {
    const policy = getAccessPolicyByUrl(`${dataPath}/${moduleName}`);
    return !(policy.GET && policy.POST);
  };

  private getEditorForViewElement = (uiElement: ViewElement): (null | React.ComponentType<BaseProps<any>>) => {
    if (isViewElementEmpty(uiElement)) {
      return null;
    } else if (isViewElementSelection(uiElement)) {
      return UiElementSelection;
    } else if (isViewElementBoolean(uiElement)) {
      return UiElementBoolean;
    } else if (isViewElementString(uiElement)) {
      return UiElementString;
    } else if (isViewElementDate(uiElement)) {
      return UiElementString;
    } else if (isViewElementNumber(uiElement)) {
      return UiElementNumber;
    } else if (isViewElementUnion(uiElement)) {
      return UIElementUnion;
    } else {
      if (process.env.NODE_ENV !== 'production') {
        console.error(`Unknown element type - ${(uiElement as any).uiType} in ${(uiElement as any).id}.`);
      }
      return null;
    }
  };

  private renderUIElement = (uiElement: ViewElement, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const isKey = (uiElement.label === keyProperty);
    const canEdit = editMode && (isNew || (uiElement.config && !isKey));

    // do not show elements w/o any value from the backend
    if (viewData[uiElement.id] == null && !editMode) {
      return null;
    } else if (isViewElementEmpty(uiElement)) {
      return null;
    } else if (uiElement.isList) {
      /* element is a leaf-list */
      return <UiElementLeafList
        key={uiElement.id}
        inputValue={viewData[uiElement.id] == null ? [] : viewData[uiElement.id]}
        value={uiElement}
        readOnly={!canEdit}
        disabled={editMode && !canEdit}
        onChange={(e) => { this.changeValueFor(uiElement.id, e); }}
        getEditorForViewElement={this.getEditorForViewElement}
      />;
    } else {
      const Element = this.getEditorForViewElement(uiElement);
      return Element != null
        ? (
          <Element
            key={uiElement.id}
            isKey={isKey}
            inputValue={viewData[uiElement.id] == null ? '' : viewData[uiElement.id]}
            value={uiElement}
            readOnly={!canEdit}
            disabled={editMode && !canEdit}
            onChange={(e) => { this.changeValueFor(uiElement.id, e); }}
          />)
        : null;
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

  private renderUIChoice = (uiElement: ViewElementChoice, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const isKey = (uiElement.label === keyProperty);

    const currentChoice = this.state.choices[uiElement.id];
    const currentCase = currentChoice && uiElement.cases[currentChoice.selectedCase];

    const canEdit = editMode && (isNew || (uiElement.config && !isKey));
    if (isViewElementChoice(uiElement)) {
      const subElements = currentCase?.elements;
      return (
        <>
          <FormControl variant="standard" key={uiElement.id} style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
            <InputLabel htmlFor={`select-${uiElement.id}`} >{uiElement.label}</InputLabel>
            <Select variant="standard"
              aria-label={uiElement.label + '-selection'}
              required={!!uiElement.mandatory}
              onChange={(e) => {
                if (currentChoice.selectedCase === e.target.value) {
                  return; // nothing changed
                }
                this.setState({ choices: { ...this.state.choices, [uiElement.id]: { ...this.state.choices[uiElement.id], selectedCase: e.target.value as string } } });
              }}
              readOnly={!canEdit}
              disabled={editMode && !canEdit}
              value={this.state.choices[uiElement.id].selectedCase}
              inputProps={{
                name: uiElement.id,
                id: `select-${uiElement.id}`,
              }}
            >
              {
                Object.keys(uiElement.cases).map(caseKey => {
                  const caseElm = uiElement.cases[caseKey];
                  return (
                    <MenuItem key={caseElm.id} value={caseKey} aria-label={caseKey}><Tooltip title={caseElm.description || ''}><div style={{ width: '100%' }}>{caseElm.label}</div></Tooltip></MenuItem>
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
            : <h3>Invalid Choice</h3>
          }
        </>
      );
    } else {
      if (process.env.NODE_ENV !== 'production') {
        console.error(`Unknown type - ${(uiElement as any).uiType} in ${(uiElement as any).id}.`);
      }
      return null;
    }
  };

  private renderUIView = (viewSpecification: ViewSpecification, dataPath: string, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
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
      } else if (isViewElementChoice(elm)) {
        acc.choices.push(elm);
      } else if (isViewElementRpc(elm)) {
        acc.rpcs.push(elm);
      } else {
        acc.elements.push(elm);
      }
      return acc;
    }, { elements: [] as ViewElement[], references: [] as ViewElement[], choices: [] as ViewElementChoice[], rpcs: [] as ViewElementRpc[] });

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
                <UIElementReference key={element.id} element={element} disabled={!isNew && (editMode || this.isPolicyViewElementForbidden(element, dataPath))} onOpenReference={(elm) => { this.navigate(`/${elm.id}`); }} />
              ))}
            </div>
          ) : null
        }
        {sections.choices.length > 0
          ? (
            <div className={classes.section}>
              {sections.choices.map(element => this.renderUIChoice(element, viewData, keyProperty, editMode, isNew))}
            </div>
          ) : null
        }
        {sections.rpcs.length > 0
          ? (
            <div className={classes.section}>
              {sections.rpcs.map(element => (
                <UIElementReference key={element.id} element={element} disabled={editMode || this.isPolicyViewElementForbidden(element, dataPath)} onOpenReference={(elm) => { this.navigate(`/${elm.id}`); }} />
              ))}
            </div>
          ) : null
        }
      </div>
    );
  };

  private renderUIViewSelector = (viewSpecification: ViewSpecification, dataPath: string, viewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) => {
    const { classes } = this.props;
    
    // group by module name
    const modules = Object.keys(viewSpecification.elements).reduce<{ [key: string]: ViewSpecification }>((acc, cur) => {
      const elm = viewSpecification.elements[cur];
      const moduleView = (acc[elm.module] = acc[elm.module] || { ...viewSpecification, elements: {} });
      moduleView.elements[cur] = elm;
      return acc;
    }, {});

    const moduleKeys = Object.keys(modules).sort();

    return (
      <div className={classes.moduleCollection}>
        {
          moduleKeys.map(key => {
            const moduleView = modules[key];
            
            return (
              <Accordion key={key} defaultExpanded={moduleKeys.length < 4} aria-label={key + '-panel'} >
                <AccordionSummaryExt expandIcon={<ExpandMoreIcon />} aria-controls={`content-${key}`} id={`header-${key}`} disabled={this.isPolicyModuleForbidden(`${key}:`, dataPath)} >
                  <Typography className={classes.heading}>{key}</Typography>
                </AccordionSummaryExt>
                <AccordionDetails>
                  {this.renderUIView(moduleView, dataPath, viewData, keyProperty, editMode, isNew)}
                </AccordionDetails>
              </Accordion>
            );
          })
        }
      </div>
    );
  };

  private renderUIViewList(listSpecification: ViewSpecification, dataPath: string, listKeyProperty: string, apiDocPath: string, listData: { [key: string]: any }[]) {
    const listElements = listSpecification.elements;
    const apiDocPathCreate = apiDocPath  ? `${location.origin}${apiDocPath
      .replace('$$$standard$$$', 'topology-netconfnode%20resources%20-%20RestConf%20RFC%208040')
      .replace('$$$action$$$', 'put')}${listKeyProperty ? `_${listKeyProperty.replace(/[\/=\-\:]/g, '_')}_` : '' }` : undefined;

    const config = listSpecification.config && listKeyProperty; // We can not configure a list with no key.

    const navigate = (path: string) => {
      this.props.history.push(`${this.props.match.url}${path}`);
    };

    const addNewElementAction = {
      icon: AddIcon, 
      tooltip: 'Add',
      ariaLabel:'add-element',
      onClick: () => {
        navigate('[]'); // empty key means new element
      },
      disabled: !config,
    };

    const addWithApiDocElementAction = {
      icon: PostAdd, 
      tooltip: 'Add',
      ariaLabel:'add-element-via-api-doc',
      onClick: () => {
        window.open(apiDocPathCreate, '_blank');
      },
      disabled: !config,
    };

    const { classes, removeElement } = this.props;

    const DeleteIconWithConfirmation: React.FC<{ disabled?: boolean; rowData: { [key: string]: any }; onReload: () => void }> = (props) => {
      const confirm = useConfirm();

      return (
        <Tooltip disableInteractive title={'Remove'} >
          <IconButton
            disabled={props.disabled}
            className={classes.button}
            aria-label="remove-element-button"
            onClick={async (e) => {
              e.stopPropagation();
              e.preventDefault();
              confirm({ title: 'Do you really want to delete this element ?', description: 'This action is permanent!', confirmationButtonProps: { color: 'secondary' }, cancellationButtonProps: { color:'inherit' } })
                .then(() => {
                  let keyId = '';
                  if (listKeyProperty && listKeyProperty.split(' ').length > 1) {
                    keyId += listKeyProperty.split(' ').map(id => props.rowData[id]).join(',');
                  } else {
                    keyId = props.rowData[listKeyProperty];
                  }
                  return removeElement(`${this.props.vPath}[${keyId}]`);
                }).then(props.onReload);
            }}
            size="large">
            <RemoveIcon />
          </IconButton>
        </Tooltip>
      );
    };

    return (
      <SelectElementTable stickyHeader idProperty={listKeyProperty} tableId={null} rows={listData} customActionButtons={apiDocPathCreate ? [addNewElementAction, addWithApiDocElementAction] : [addNewElementAction]} columns={
        Object.keys(listElements).reduce<ColumnModel<{ [key: string]: any }>[]>((acc, cur) => {
          const elm = listElements[cur];
          if (elm.uiType !== 'object' && listData.every(entry => entry[elm.label] != null)) {
            if (elm.label !== listKeyProperty) {
              acc.push(elm.uiType === 'boolean'
                ? { property: elm.label, type: ColumnType.boolean }
                : elm.uiType === 'date'
                  ? { property: elm.label, type: ColumnType.date }
                  : { property: elm.label, type: elm.uiType === 'number' ? ColumnType.numeric : ColumnType.text });
            } else {
              acc.unshift(elm.uiType === 'boolean'
                ? { property: elm.label, type: ColumnType.boolean }
                : elm.uiType === 'date'
                  ? { property: elm.label, type: ColumnType.date }
                  : { property: elm.label, type: elm.uiType === 'number' ? ColumnType.numeric : ColumnType.text });
            }
          }
          return acc;
        }, []).concat([{
          property: 'Actions', disableFilter: true, disableSorting: true, type: ColumnType.custom, customControl: (({ rowData }) => {
            return (
              <DeleteIconWithConfirmation disabled={!config} rowData={rowData} onReload={() => this.props.vPath && this.props.reloadView(this.props.vPath)} />
            );
          }),
        }])
      } onHandleClick={(ev, row) => {
        ev.preventDefault();
        let keyId = '';
        if (listKeyProperty && listKeyProperty.split(' ').length > 1) {
          keyId += listKeyProperty.split(' ').map(id => encodeURIComponent(String(row[id]))).join(',');
        } else {
          keyId = encodeURIComponent(String(row[listKeyProperty]));
        }
        if (listKeyProperty) {
          navigate(`[${keyId}]`); // Do not navigate without key.
        }
      }} ></SelectElementTable>
    );
  }

  private renderUIViewRPC(inputViewSpecification: ViewSpecification | undefined, dataPath: string, inputViewData: { [key: string]: any }, outputViewData: { [key: string]: any }, keyProperty: string | undefined, editMode: boolean, isNew: boolean) {
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
        console.error('Object should not appear in RPC view !');
      } else if (isViewElementChoice(elm)) {
        acc.choices.push(elm);
      } else if (isViewElementRpc(elm)) {
        console.error('RPC should not appear in RPC view !');
      } else {
        acc.elements.push(elm);
      }
      return acc;
    }, { elements: [] as ViewElement[], references: [] as ViewElement[], choices: [] as ViewElementChoice[], rpcs: [] as ViewElementRpc[] })
      || { elements: [] as ViewElement[], references: [] as ViewElement[], choices: [] as ViewElementChoice[], rpcs: [] as ViewElementRpc[] };

    sections.elements = sections.elements.sort(orderFunc);

    return (
      <>
        <div className={classes.section} />
        { sections.elements.length > 0
          ? (
            <div className={classes.section}>
              {sections.elements.map(element => this.renderUIElement(element, inputViewData, keyProperty, editMode, isNew))}
            </div>
          ) : null
        }
        { sections.choices.length > 0
          ? (
            <div className={classes.section}>
              {sections.choices.map(element => this.renderUIChoice(element, inputViewData, keyProperty, editMode, isNew))}
            </div>
          ) : null
        }
        <Button color="inherit" onClick={() => {
          const resultingViewData = inputViewSpecification && this.collectData(inputViewSpecification.elements);
          this.props.executeRpc(this.props.vPath!, resultingViewData);
        }} >Exec</Button>
        <div className={classes.objectReult}>
          {outputViewData !== undefined
            ? renderObject(outputViewData)
            : null
          }
        </div>
      </>
    );
  }

  private renderBreadCrumps() {
    const { editMode, isNew, isNewSubElement } = this.state;
    const { displaySpecification, vPath, nodeId } = this.props;
    const pathParts = splitVPath(vPath!, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
    
    let lastPath = '/configuration';
    let basePath = `/configuration/${nodeId}`;
    
    return (
      <div className={this.props.classes.header}>
        <div>
          <Breadcrumbs aria-label="breadcrumbs">
            <Link underline="hover" color="inherit" href="#" aria-label="back-breadcrumb"
              onClick={(ev: React.MouseEvent<HTMLElement>) => {
                ev.preventDefault();
                this.props.history.push(lastPath);
              }}>Back</Link>
            <Link underline="hover" color="inherit" href="#"
              aria-label={nodeId + '-breadcrumb'}
              onClick={(ev: React.MouseEvent<HTMLElement>) => {
                ev.preventDefault();
                this.props.history.push(`/configuration/${nodeId}`);
              }}><span>{nodeId}</span></Link>
            {
              pathParts.map(([prop, key], ind) => {
                const path = `${basePath}/${prop}`;
                const keyPath = key && `${basePath}/${prop}[${key}]`;
                const propTitle = prop.replace(/^[^:]+:/, '');
                const ret = (
                  <span key={ind}>
                    <Link underline="hover" color="inherit" href="#"
                      aria-label={propTitle + '-breadcrumb'}
                      onClick={(ev: React.MouseEvent<HTMLElement>) => {
                        ev.preventDefault();
                        this.props.history.push(path);
                      }}><span>{propTitle}</span></Link>
                    {
                      keyPath && <Link underline="hover" color="inherit" href="#"
                        aria-label={key + '-breadcrumb'}
                        onClick={(ev: React.MouseEvent<HTMLElement>) => {
                          ev.preventDefault();
                          this.props.history.push(keyPath);
                        }}>{`[${key && key.replace(/\%2C/g, ',')}]`}</Link> || null
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
          <Fab color="secondary" aria-label="back-button" className={this.props.classes.fab} onClick={async () => {
            if (this.props.vPath) {
              if (isNewSubElement || isNew) {
                const index = this.props.vPath.lastIndexOf('[]');
                const newVPath = this.props.vPath.substring(0, index + ( isNewSubElement ? 2 : 0 ));
                this.props.history.replace(`/configuration/${nodeId}/${newVPath}`);
              } else {
                await this.props.reloadView(this.props.vPath);
              }
            }
            this.setState({ editMode: false });
          }} ><ArrowBack /></Fab>
        ) || null}
        { /* do not show edit if this is a list or it can't be edited */
          displaySpecification.displayMode === DisplayModeType.displayAsObject && displaySpecification.viewSpecification.canEdit && (<div>
            <Fab color="secondary" aria-label={editMode ? 'save-button' : 'edit-button'} className={this.props.classes.fab} onClick={() => {
              if (this.state.editMode && this.props.vPath) {
                // ensure only active choices will be contained
                const resultingViewData = this.collectData(displaySpecification.viewSpecification.elements);
                this.props.onUpdateData(this.props.vPath!, resultingViewData);

                const index = this.props.vPath.lastIndexOf('[]');
                const newVPath = this.props.vPath.substring(0, index + ( isNewSubElement ? 2 : 0 ));
                this.props.history.replace(`/configuration/${nodeId}/${newVPath}`);
              }
              this.setState({ editMode: !editMode });
            }}>
              { editMode
                ? isNewSubElement
                  ? <CheckIcon />
                  : <SaveIcon />
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
      throw new Error('ListKex ot view not specified.');
    }

    return (
      <div className={this.props.classes.container}>
        <SelectElementTable stickyHeader idProperty={listKeyProperty} tableId={null} rows={listData} columns={
          Object.keys(listSpecification.elements).reduce<ColumnModel<{ [key: string]: any }>[]>((acc, cur) => {
            const elm = listSpecification.elements[cur];
            if (elm.uiType !== 'object' && listData.every(entry => entry[elm.label] != null)) {
              if (elm.label !== listKeyProperty) {
                acc.push({ property: elm.label, type: elm.uiType === 'number' ? ColumnType.numeric : ColumnType.text });
              } else {
                acc.unshift({ property: elm.label, type: elm.uiType === 'number' ? ColumnType.numeric : ColumnType.text });
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
            ? this.renderUIViewList(ds.viewSpecification, ds.dataPath!, ds.keyProperty!, ds.apidocPath!, viewData)
            : ds.displayMode === DisplayModeType.displayAsRPC
              ? this.renderUIViewRPC(ds.inputViewSpecification, ds.dataPath!, viewData!, outputData, undefined, true, false)
              : ds.displayMode === DisplayModeType.displayAsMessage
                ? this.renderMessage(ds.renderMessage)
                : this.renderUIViewSelector(ds.viewSpecification, ds.dataPath!, viewData!, ds.keyProperty, editMode, isNew)
        }
      </div >
    );
  }

  private renderMessage(renderMessage: string) {
    return (
      <div className={this.props.classes.container}>
        <h4>{renderMessage}</h4>
      </div>
    );
  }

  private renderCollectingData() {
    return (
      <div className={this.props.classes.outer}>
        <div className={this.props.classes.inner}>
          <Loader />
          <h3>Processing ...</h3>
        </div>
      </div>
    );
  }

  render() {
    console.log('ConfigurationApplication.render()', this.props);
    return this.props.collectingData || !this.state.viewData
      ? this.renderCollectingData()
      : this.props.listSpecification
        ? this.renderValueSelector()
        : this.renderValueEditor();
  }
}

export const ConfigurationApplication = withStyles(styles)(withRouter(connect(mapProps, mapDispatch)(ConfigurationApplicationComponent)));
export default ConfigurationApplication;
>>>>>>> CHANGE (5418ff ODLUX Update)

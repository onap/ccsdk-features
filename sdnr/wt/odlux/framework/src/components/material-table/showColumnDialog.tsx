/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
import { Button, FormControlLabel, Popover, Switch, Typography } from '@mui/material';
import { connect, Connect, IDispatcher } from '../../flux/connect';

import { ColumnModel } from './columnModel';
import { IApplicationStoreState } from '../../store/applicationStore';
import { TableSettingsColumn } from '../../models/settings';
import { updateTableSettings } from '../../actions/settingsAction';

const mapStateToProps = (state: IApplicationStoreState) => ({
    settings: state.framework.applicationState.settings,
    settingsDoneLoading: state.framework.applicationState.settings.isInitialLoadDone
});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({
    saveToSettings: (tableName: string, columns: TableSettingsColumn[]) => dispatcher.dispatch(updateTableSettings(tableName, columns))
})

type DialogProps = {
    columns: ColumnModel<{}>[],
    settingsName: string | null,
    anchorEl: HTMLElement | null;
    hideColumns: (columnNames: string[]) => void
    showColumns: (columnNames: string[]) => void
    onClose(): void

} & Connect<typeof mapStateToProps, typeof mapDispatchToProps>;

        //TODO: figure out why everything gets triggered twice...

const ShowColumnsDialog: React.FunctionComponent<DialogProps> = (props) => {

    const savedSettings = props.settingsName && props.settings.tables[props.settingsName];

    const [checkedColumns, setCheckedColumns] = React.useState<{ property: string, display: boolean, title: string | undefined }[]>([]);

    const open = Boolean(props.anchorEl);
    const allColumnNames = props.columns.map(e => e.property);

    React.useEffect(() => {

        createHideShowSelection();

    }, []);

    React.useEffect(() => {

        createHideShowSelection();

    }, [props.settings.isInitialLoadDone]);


    const createHideShowSelection = () => {
        let columns = props.columns.map(e => { return { property: e.property, display: !Boolean(e.hide), title: e.title } });


        if (savedSettings) {

            if (columns.length !== savedSettings.columns.length) {
                console.error("saved column length does not match current column length. Maybe a settings entry got wrongly overridden?")
            }

            //overwrite column data with settings
            savedSettings?.columns.forEach(el => {
                let foundIndex = columns.findIndex(e => e.property == el.property);
                if (columns[foundIndex] !== undefined)
                    columns[foundIndex].display = el.displayed;
            });

        } else {
            console.warn("No settingsName set, changes will not be saved.")
        }

        setCheckedColumns(columns);

        const hideColumns = columns.filter(el => !el.display).map(e => e.property);
        props.hideColumns(hideColumns);
    }


    const handleChange = (propertyName: string, checked: boolean) => {
        if (!checked) {
            props.hideColumns([propertyName]);
        } else {
            props.showColumns([propertyName])

        }
     
        let updatedList = checkedColumns.map(item => {
            if (item.property == propertyName) {
                return { ...item, display: checked }; 
            }
            return item; 
        });

        setCheckedColumns(updatedList);
    };

    const onHideAll = () => {

        switchCheckedColumns(false);
        props.hideColumns(allColumnNames);
    }

    const onShowAll = () => {

        switchCheckedColumns(true);
        props.showColumns(allColumnNames);
    }

    const onClose = () => {

        const tableColumns: TableSettingsColumn[] = checkedColumns.map(el => {
            return {
                property: el.property,
                displayed: el.display
            }
        });

        if (props.settingsName) {
            props.saveToSettings(props.settingsName, tableColumns);
        }
        props.onClose();

    }

    const switchCheckedColumns = (changeToValue: boolean) => {
        let updatedList = checkedColumns.map(item => {
            return { ...item, display: changeToValue };
        });

        setCheckedColumns(updatedList);

    }

    return (<Popover open={open} onClose={onClose}
        anchorEl={props.anchorEl}
        anchorOrigin={{
            vertical: 'top',
            horizontal: 'left',
        }} >
        <div>
            <Typography fontWeight={600} style={{ margin: 10 }} >Hide / Show Columns</Typography>
        </div>
        <div style={{ display: "flex", flexDirection: "column", margin: 10 }}>
            {
                checkedColumns?.map((el, i) => {

                    return <>

                        <FormControlLabel
                            value="end"
                            key={"hide-show-column-"+i}
                            aria-label={"hide-or-show-column-button"}
                            control={<Switch color="secondary" checked={el.display} onChange={e => handleChange(el.property, e.target.checked)} />}
                            label={el.title || el.property}
                            labelPlacement="end"
                        />
                    </>
                })
            }
            <div style={{ display: "flex", flexDirection: "row", justifyContent: "space-between" }}>
                <Button color="secondary" aria-label="hide-all-columns-button" onClick={(e) => onHideAll()}>Hide all</Button>
                <Button color="secondary" aria-label="show-all-columns-button" onClick={(e) => onShowAll()}>Show all</Button>
            </div>
        </div>
    </Popover>)
}

export default connect(mapStateToProps, mapDispatchToProps)(ShowColumnsDialog);

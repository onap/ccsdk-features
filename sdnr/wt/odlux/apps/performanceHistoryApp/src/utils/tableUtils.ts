import { ColumnType, ColumnModel } from '../../../../framework/src/components/material-table';

export const addColumnLabels = <T>(name: string, title: string, disableFilter = true, disableSorting = true): ColumnModel<T> => {
  return { property: name as keyof T, title: title, type: ColumnType.text, disableFilter: disableFilter, disableSorting: disableSorting };
}

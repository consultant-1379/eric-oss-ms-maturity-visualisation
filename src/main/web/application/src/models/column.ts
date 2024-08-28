///
/// COPYRIGHT Ericsson 2021
///
///
///
/// The copyright to the computer program(s) herein is the property of
///
/// Ericsson Inc. The programs may be used and/or copied only with written
///
/// permission from Ericsson Inc. or in accordance with the terms and
///
/// conditions stipulated in the agreement/contract under which the
///
/// program(s) have been supplied.
///


import { ColumnType } from './column-type';
import { Cell } from './index';

export interface Column {
  id: string;
  columnDef: string;
  header: string;
  headerToolTip?:string;
  url?: string;
  gatingEnabled: boolean;
  hide: boolean;
  isExpanded: boolean;
  subColumns?: Column[];
  type: ColumnType;
  rules?: string[];
  cell(cells: Array<Cell>): Cell;
}

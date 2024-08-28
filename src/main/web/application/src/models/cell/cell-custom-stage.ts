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


import {CellTypes} from './cell-type';
import {Cell} from './cell';
import {CellWithRule} from './cell-with-rule';
import {isEmpty} from 'lodash';

export class CellCustomStage implements Cell, CellWithRule{
  id!: number;
  columnId!: number;
  name!: string;
  rules?:string[];
  type: CellTypes = CellTypes.cellCustomStage;

  hasRules(): boolean {
    return !isEmpty(this.rules);
  }
}

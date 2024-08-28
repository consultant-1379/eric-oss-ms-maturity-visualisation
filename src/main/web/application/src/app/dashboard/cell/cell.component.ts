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



import {Component, Input, OnInit} from '@angular/core';
import {Cell, CellStage, CellText, CellTypes} from 'src/models';
import {CellCustomStage} from '../../../models/cell/cell-custom-stage';

@Component({
  selector: 'cell-component',
  templateUrl: './cell.component.html',
  styleUrls: ['./cell.component.css']
})
export class CellComponent implements OnInit {

  @Input() cell!: Cell;
  cellTypes = CellTypes;

  constructor() {
  }

  ngOnInit() {
  }

  getCustomStageCell(): CellCustomStage {
    return this.cell as CellCustomStage;
  }

  getStageCell(): CellStage {
    return this.cell as CellStage;
  }

  getTextCell(): CellText {
    return this.cell as CellText;
  }
}

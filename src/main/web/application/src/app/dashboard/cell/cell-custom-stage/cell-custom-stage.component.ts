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


import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CellCustomStage } from 'src/models';
import { CellDialogComponent } from '../cell-dialog/cell-dialog.component';

@Component({
  selector: 'cell-custom-stage',
  templateUrl: './cell-custom-stage.component.html',
  styleUrls: ['./cell-custom-stage.component.css']
})
export class CellCustomStageComponent{
  @Input()
    cell!: CellCustomStage;

    constructor(public dialog: MatDialog) {
    }

    openRulesDialog() {
        if (this.cell.hasRules()) {
            this.dialog.open(CellDialogComponent, {
                data: {
                    rules: this.cell.rules,
                    title: 'Executed rules:'
                }
            });
        }
    }

    getClass() {
        return this.cell.hasRules() ? 'cell-rules-dialog' : '';
    }

}

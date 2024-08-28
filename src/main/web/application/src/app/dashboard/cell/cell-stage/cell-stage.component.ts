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
import { Constants } from 'src/constants/constants';
import { CellStage } from 'src/models';
import { CellDialogComponent } from '../cell-dialog/cell-dialog.component';

@Component({
    selector: 'cell-stage',
    templateUrl: './cell-stage.component.html',
    styleUrls: ['./cell-stage.component.scss']
})
export class CellStageComponent implements OnInit {
    @Input() cell!: CellStage;
    text: string = '';
    classes: string = 'mat-card-stage';
    name: string = Constants.NAME;
    status: string = Constants.STATUS;
    getStatus: string = '';

    constructor(public dialog: MatDialog) {
    }

    ngOnInit(): void {
        if(this.cell.skippedStage){
                       this.text = `Skipped`;
                        this.addClasses('skipped');
                       this.getStatus = `The stage ${this.cell.stageName} is skipped`;
        } else if (this.cell.missing) {
            this.text = `Missing`;
            this.addClasses('missing');
            this.getStatus = `Stage ${this.cell.stageName} not available`;
        } else if (!this.cell.mandatoryRulesCount) {
            this.text = `No rules`;
            this.addClasses('not-rules');
            this.getStatus = 'NA';
        } else if (this.cell.mandatoryRulesCount && this.cell.coveredRules) {
            this.text = `${this.cell.coveredRules} / ${this.cell.mandatoryRulesCount}`;
            if (this.cell.coveredRules === this.cell.mandatoryRulesCount) {
                this.addClasses('full-covered');
                this.getStatus = 'All Rules Executed';
            } else {
                if(this.cell.conditionalStageStatus) {
                    switch(this.cell.conditionalStageStatus) {
                        case 'SUCCESS' : {
                            this.addClasses('full-covered');
                            break;
                        }
                        case 'FAILURE': {
                            this.addClasses('not-covered');
                            break;
                        }
                        case 'PARTIAL': {
                            this.addClasses('part-covered');
                            break;
                        }
                    }
                } else {
                    this.addClasses('part-covered');
                }
                this.getStatus = 'Rules Skipped';
            }
        } else if (this.cell.mandatoryRulesCount && !this.cell.coveredRules) {
            this.addClasses('not-covered');
            this.text = `0 / ${this.cell.mandatoryRulesCount}`;
            this.getStatus = 'Rules Not Executed';
        }
        if (this.cell.hasRules()) {
            this.addClasses('cell-rules-dialog');
        }
    }

    openRulesDialog() {
        if (this.cell.hasRules()) {
            this.dialog.open(CellDialogComponent, {
                data: {
                    rules: this.cell.missingRules,
                    title: 'Missing rules:'
                }
            });
        }
    }

    private addClasses(value: string): void {
        this.classes = this.classes + ' ' + value;
    }

    getStageTooltip() {
        return `${this.name} ${this.cell.stageName}  \n ${this.status} ${this.getStatus}`;
    }
}

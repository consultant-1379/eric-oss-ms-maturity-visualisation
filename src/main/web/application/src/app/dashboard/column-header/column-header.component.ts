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

@Component({
    selector: 'column-header',
    templateUrl: './column-header.component.html',
    styleUrls: ['./column-header.component.scss']
})
export class ColumnHeaderComponent implements OnInit{

    @Input() header!: string;
    @Input() url?: string;
    @Input() gatingEnabled = false;
    @Input() headerToolTip?: string;
     headerToolTipComp : string = "";

    constructor(public dialog: MatDialog) {
    }
    ngOnInit(): void {
        this.headerToolTipComp =this.headerToolTip?this.headerToolTip:"";
    }

}

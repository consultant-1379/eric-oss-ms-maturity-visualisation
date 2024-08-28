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


import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'cell-dialog',
    templateUrl: './cell-dialog.component.html',
    styleUrls: ['./cell-dialog.component.css']
})
export class CellDialogComponent {

    constructor(public dialogRef: MatDialogRef<CellDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: {
                    rules: string[];
                    title: string
                }) {
    }

    close(): void {
        this.dialogRef.close();
    }
}

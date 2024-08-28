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


import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
    selector: 'app-dashboard-help-dialog',
    templateUrl: './dashboard-filter-help-dialog.component.html',
    styleUrls: ['./dashboard-filter-help-dialog.component.css']
})
export class DashboardFilterHelpDialogComponent {
    constructor(public dialogRef: MatDialogRef<DashboardFilterHelpDialogComponent>, 
        @Inject(MAT_DIALOG_DATA) public data: any) {}

    close(): void {
        this.dialogRef.close();
    }
}

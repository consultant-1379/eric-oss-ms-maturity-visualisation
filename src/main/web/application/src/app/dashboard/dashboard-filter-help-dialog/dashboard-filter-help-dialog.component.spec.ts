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


import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DashboardFilterHelpDialogComponent } from './dashboard-filter-help-dialog.component';

describe('DashboardHelpDialogComponent', () => {
  let component: DashboardFilterHelpDialogComponent;
  let fixture: ComponentFixture<DashboardFilterHelpDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DashboardFilterHelpDialogComponent],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialogRef, useValue: {} }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardFilterHelpDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should', async(() => {
    //spyOn(component, 'close');
  
    let button = fixture.debugElement.nativeElement.querySelector('mat-dialog-content');
    button.click();
    // Re-render the Component
    fixture.detectChanges();
  
    fixture.whenStable().then(() => {
      expect(component.close).toBeTruthy();
    });
  }));
});

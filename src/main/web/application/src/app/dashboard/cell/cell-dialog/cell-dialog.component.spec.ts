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


import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import { MAT_DIALOG_DATA,MatDialogRef } from '@angular/material/dialog';

import {CellDialogComponent} from './cell-dialog.component';

describe('CellDialogComponent', () => {
  let component: CellDialogComponent;
  let fixture: ComponentFixture<CellDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CellDialogComponent ],
      providers: [{provide : MatDialogRef, useValue: {}},
        {provide : MAT_DIALOG_DATA, useValue: {}}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CellDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

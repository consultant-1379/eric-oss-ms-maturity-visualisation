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

import {CellHeaderComponent} from './cell-header.component';
import {CellText} from 'src/models';
import { MatDialogModule } from '@angular/material/dialog';

describe('CellHeaderComponent', () => {
  let component: CellHeaderComponent;
  let fixture: ComponentFixture<CellHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CellHeaderComponent ],
      imports : [MatDialogModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CellHeaderComponent);
    component = fixture.componentInstance;
    component.cell = new CellText();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

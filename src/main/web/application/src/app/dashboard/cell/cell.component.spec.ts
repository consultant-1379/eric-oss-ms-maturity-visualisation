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
import { CellStage } from 'src/models/cell/cell-stage';

import {CellComponent} from './cell.component';
//import {CellStatus} from '../../../../models/cells/cell-status';

describe('CellComponent', () => {
  let component: CellComponent;
  let fixture: ComponentFixture<CellComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CellComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CellComponent);
    component = fixture.componentInstance;
    component.cell = new CellStage();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

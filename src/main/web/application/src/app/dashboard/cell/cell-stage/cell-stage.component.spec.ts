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

import {CellStageComponent} from './cell-stage.component';
import {CellStage} from 'src/models';
import { MatDialogModule } from '@angular/material/dialog';

describe('CellHeaderComponent', () => {
  let component: CellStageComponent;
  let fixture: ComponentFixture<CellStageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CellStageComponent ],
      imports: [MatDialogModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CellStageComponent);
    component = fixture.componentInstance;
    component.cell = new CellStage;
    component.cell.missing = true;
    component.cell.missingRules = ['No'];
    fixture.detectChanges();
  });
  it(`No rules`, () => {
    fixture = TestBed.createComponent(CellStageComponent);
            component = fixture.componentInstance;
            component.cell = new CellStage;
    component.cell.missing = false;
    fixture.detectChanges();
    expect(component.text).toBe('No rules');  
  });
  it('to be 2 / 2', () => {
    fixture = TestBed.createComponent(CellStageComponent);
    component = fixture.componentInstance;
    component.cell = new CellStage;
    component.cell.missing = false;
    component.cell.mandatoryRulesCount = 2;
    component.cell.coveredRules = 2;
    fixture.detectChanges();
    expect(component.text).toBe('2 / 2');
    expect(component.getStatus).toBe('OK');
  });
  it('to be 2 / 2', () => {
    fixture = TestBed.createComponent(CellStageComponent);
    component = fixture.componentInstance;
    component.cell = new CellStage;
    component.cell.missing = false;
    component.cell.mandatoryRulesCount = 2;
    component.cell.coveredRules = 3;
    fixture.detectChanges();
    expect(component.getStatus).toBe('Skipped');
  });
  it(`should be Not Ok`, () => {
    fixture = TestBed.createComponent(CellStageComponent);
            component = fixture.componentInstance;
            component.cell = new CellStage;
    component.cell.mandatoryRulesCount = 1;
    fixture.detectChanges();
    expect(component.getStatus).toBe('Not OK');  
  });
});

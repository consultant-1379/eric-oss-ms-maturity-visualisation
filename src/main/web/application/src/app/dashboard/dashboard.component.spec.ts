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


import { async, ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { Cell, CellEmpty, CellText, Column, Job, Report, Row } from 'src/models';
import { ReportService } from 'src/services/report.service';
import { DashboardComponent } from './dashboard.component';
import { TableMapperService } from 'src/services/table-mapper.service';
import { ColumnType } from 'src/models/column-type';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockTableMapperService: Partial<TableMapperService>;
  let appType = 'java';

  let mockReportService: Partial<ReportService>;
  let report = new Report();
  report.appType = 'java';
  report.jobType = 'Publish';
  let job = new Job();
  job.buildNo = '303';
  job.jobName = 'eric-oss-5gpmevent-filetrans-proc_Publish';
  job.jobUrl = 'https://fem1s11-eiffel216.eiffel.gic.ericsson.se:8443/jenkins/job/eric-oss-5gpmevent-filetrans-proc_Publish/';

  let rows: Row[] = [];
  let columns: Column[] = [];
  let cells: Array<Cell> = new Array<Cell>(new CellEmpty());
  let row = new Row();
  let cell = new CellText();
  cell.columnId = 1;
  cell.id = 1;
  cell.text = 'Text';
  row.cells.push(cell);
  rows.push(row);
  let column = {
    id: '1',
    columnDef: '2',
    header: 'qw',
    hide: false,
    isExpanded: true,
    type: ColumnType.COLUMN,
    cell: function (cells: Cell[]): Cell {
      return cell;
    }
  };
  columns.push(column);

  beforeEach(waitForAsync(() => {
    mockTableMapperService = {
      getRows(): Row[] { return rows; },
      getColumns(): Column[] { return columns; }
    }
    mockReportService = {

    }
    let matDialogService: jasmine.SpyObj<MatDialog>;
    matDialogService = jasmine.createSpyObj<MatDialog>('MatDialog', ['open']);
    TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      providers: [{ provide: TableMapperService, useValue: mockTableMapperService },
      { provide: ReportService, useValue: mockReportService },
      { provide: MatDialog, useValue: matDialogService }]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    mockTableMapperService = TestBed.inject(TableMapperService);
    component.report = report;
    fixture.detectChanges();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });
  it('should expand', () => {
    component.expand(column);
    fixture.detectChanges();
    expect(columns[0].hide).toBe(false);
  });
});

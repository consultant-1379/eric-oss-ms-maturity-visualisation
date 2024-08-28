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


import { HttpHeaders } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute } from '@angular/router';
import { get, has, isEmpty } from 'lodash';
import { Observable } from 'rxjs/internal/Observable';
import { Constants } from 'src/constants/constants';
import { Cell, CellEmpty, CellText, Column } from 'src/models';
import { LoaderService } from 'src/services/loader.service';
import { ColumnType } from '../../models/column-type';
import { ReportService } from '../../services/report.service';
import { TableMapperService } from '../../services/table-mapper.service';
import { CellDialogComponent } from './cell/cell-dialog/cell-dialog.component';
import {
    DashboardFilterHelpDialogComponent
} from './dashboard-filter-help-dialog/dashboard-filter-help-dialog.component';

@Component({
    selector: 'dashboard-component',
    styleUrls: ['dashboard.component.css'],
    templateUrl: 'dashboard.component.html',
})

export class DashboardComponent implements OnInit {

   @Input() techs!: string;
   @Input() count : string | undefined;
    page: number = 0;
    size: number = 50;
    nextSlice: boolean = true;
    report: any = {}
    dataSource = new MatTableDataSource<Cell[]>();
    columns: Column[] = [];
    filter: string = "";
    columnFilter = '';
    alignColumn: string = Constants.BUILD_SUCCESS_PERC;
    alignText: string = Constants.ALIGN_TEXT;
    doStagesAligned: Record<string, number> = {};
    stagesAlignedPercentage: Record<string, number> = {};
    appType!: string;
    type!: string;
    product: string;
    loader$ !: Observable<string>
    constructor(
        private reportService: ReportService,
        private route: ActivatedRoute,
        private dialog: MatDialog,
        public loaderService: LoaderService,
        private tableMapperService: TableMapperService) {
        this.product = this.route.snapshot.paramMap.get('product') || '';
        this.initFilterPredicate();
    }

    ngOnInit(): void {
        [this.appType, this.type] = this.techs.split(' ');
        this.dataSource.data = [];
        this.columns = [];
        this.loader$ = this.loaderService.getLoaderObserver(this.techs);
    }

    expand(column: Column) {
        column.isExpanded = !column.isExpanded;
        const ids: string[] = [];
        column.subColumns?.map(subColumn => subColumn.id)
            .forEach(id => ids.push(id));
        ids.push(column.id + 'start');
        ids.push(column.id + 'end');
        this.columns.forEach(columnData => {
            if (ids.includes(columnData.id)) {
                columnData.hide = !columnData.hide;
            }
        });
    }

    getClassName(column: Column, row?: any) {
        let className = 'parent-stage';
        switch (column.type) {
            case ColumnType.SPACER: {
                className = 'mat-column-column-spacer';
                break;
            }
            case ColumnType.SPACER_START: {
                className = 'sub-stage-spacer sub-stage-spacer-start';
                break;
            }
            case ColumnType.SPACER_END: {
                className = 'sub-stage-spacer sub-stage-spacer-end';
                break;
            }
            case ColumnType.SUB_COLUMN: {
                className = 'sub-stage';
                className = className + ' va-stage';
                break;
            }
            case ColumnType.COLUMN: {
                className = '';
                if(column.id === '6') className = ' sonar-cell'
                break;
            }
        }

        if(className.includes("parent-stage")) className = className + ' va-stage';
        if (row && row[0].text === 'first-header-spacer') {
            className = className + ' header-spacer';
        }

        if (!row && this.hasRules(column)) {
            className = className + ' cell-rules-dialog';
        }

        return className;
    }

    getFilteredColumns(): string[] {
        if (this.columns) {
            return this.columns.filter(column => !column.hide)
            .filter(column => column.id === '1' || column.id === '2' || column.header.toLowerCase().includes(this.columnFilter.trim().toLowerCase()))
            .map(column => column.columnDef);
        }
        return [];
    }

    addSubColumns(column: Column) {
        this.columns.push(column);
        column?.subColumns?.forEach((subColumn, index, col) => {
            if (index === 0) {
                this.columns.push(this.createColumnSpacer(column.id + 'start', ColumnType.SPACER_START, subColumn.hide, subColumn.gatingEnabled));
                this.columns.push(subColumn);
                if (index === col.length - 1) {
                    this.columns.push(this.createColumnSpacer(column.id + 'end', ColumnType.SPACER_END, subColumn.hide, subColumn.gatingEnabled));
                }
            } else if (column.subColumns && index === (column.subColumns.length - 1)) {
                this.columns.push(subColumn);
                this.columns.push(this.createColumnSpacer(column.id + 'end', ColumnType.SPACER_END, subColumn.hide, subColumn.gatingEnabled));
            } else {
                this.columns.push(subColumn);
            }
        });
    }

    searchProperty(element: Cell, propertyName: string, filterString: string): boolean {
        if (has(element, propertyName) && get(element, propertyName) != null) {
            for (const value of filterString.split('||')) {
                if (get(element, propertyName).toLocaleLowerCase().indexOf(value.trim()) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    onFilterChange(): void {
        this.setDatasourceFilter();
    }

    setDatasourceFilter() {
        this.dataSource.filter = this.filter?.trim()?.toLowerCase();
    }

    openFilterHelpDialog(isColumnFilter: boolean) {
        this.dialog.open(DashboardFilterHelpDialogComponent, {
            data: {
                columnFilter: isColumnFilter
            }
        });
    }

    openRulesDialog(column: Column) {
        if (this.hasRules(column)) {
            let sortColumnRules = column.rules?.sort((a, b) => {
                if (a < b) {
                    return -1;
                }
                if (b < a) {
                    return 1;
                }
                return 0;
            });
            this.dialog.open(CellDialogComponent, {
                data: {
                    rules: sortColumnRules,
                    title: `${column.header} rules:`
                }
            });
        }
    }

    getVATooltip(stage: string): string {
        const value = Math.abs(this.doStagesAligned[stage]) ;
        return `${Constants.VA_COLUMN_TOOLTIP} ${this.doStagesAligned[stage] == 0 ? ' is stable' : (this.doStagesAligned[stage] > 0 ? `grown by ${value} rules overall` : `reduced by ${value} rules overall` )}`;
    }

    private hasRules(column: Column): boolean {
        return !isEmpty(column.rules);
    }

    private addColumn(column: Column) {
        if (column?.subColumns && column?.subColumns?.length > 0) {
            this.addSubColumns(column);
        } else {
            this.columns.push(column);
        }
    }

    private createColumnSpacer(id: string,
                               type: ColumnType,
                               hide: boolean, gatingEnabled: boolean): Column {
        return {
            columnDef: `${id}`,
            id: `${id}`,
            header: 'column-spacer',
            hide,
            gatingEnabled,
            isExpanded: false,
            type,
            cell: (element: Array<Cell>) => {
                return new CellEmpty();
            }
        };
    }

    private createEmptyCells(numCell: number): Cell[] {

        const cells: Cell[] = new Array<Cell>();
        for (let i = 0; i <= numCell; i++) {
            const cell = new CellText();
            cell.columnId = i;
            if (i === 0) {
                cell.text = 'first-header-spacer';
            } else {
                cell.text = 'header-spacer';
            }
            cells.push(cell);
        }
        return cells;
    }

    private initFilterPredicate() {
        this.dataSource.filterPredicate = (data: Cell[], filterString) => {
            for (const cell of data) {
                if (this.searchProperty(cell, 'text', filterString)) {
                    return true;
                }
                if (this.searchProperty(cell, 'name', filterString)) {
                    return true;
                }
                if (this.isNotMissingStage(cell) && this.searchProperty(cell, 'stageName', filterString)) {
                    return true;
                }
            }
            return false;
        };
    }

    private isNotMissingStage(element: Cell): boolean {
        return has(element, 'missing') && !get(element, 'missing');
    }

    mapDataToTable(){
        this.report = this.reportService.getData(this.product+'-'+this.techs);
        if(this.report){
            this.nextSlice = false;
            const {columns, dataSource} = this.reportService.getData(this.product+'-'+this.techs);
            this.columns = columns;
            this.dataSource = dataSource;
            this.dataSource._updateChangeSubscription();
        } else{
            this.getRecordsByRecursion(this.page);
        }

    }

    performSomeMagic(input: any, data: Record<string, number>, page: number) {
        Object.keys(input).forEach(stage => {
            if(data[stage]){
                data[stage] += input[stage];
                data[stage] /= page;
            }
            else data[stage] = input[stage];
        })
    }

    getRecordsByRecursion(page: number): any{
        const headers = new HttpHeaders().set(Constants.HTTP_HEADER_NAME, this.techs);
        return this.reportService.getProductReports(this.product as string, this.appType, this.type, page, this.size, headers)
            .subscribe({
                next: value => {
                    this.nextSlice = value.nextSlice
                    const reportData : any = value.reports;
                    if (reportData?.jobs?.length > 0) {
                        this.performSomeMagic(reportData?.isStageAligned, this.doStagesAligned, page+1);
                        this.performSomeMagic(reportData?.totalStagePercentage, this.stagesAlignedPercentage, page+1);
                        this.report = {
                            ...reportData,
                            jobs: [...this.report?.jobs ?? [], ...reportData?.jobs],
                            page: page + 1
                        }
                        if(this.report){
                            this.dataSource.data = [];
                            this.columns = [];
                            const rows = this.tableMapperService.getRows(this.report);
                            const columns = this.tableMapperService.getColumns(this.report);
                            columns.forEach(column => this.addColumn(column));
                            this.dataSource.data.push(this.createEmptyCells(this.columns.length));
                            rows.forEach(row => this.dataSource.data.push(row.cells));
                            this.dataSource._updateChangeSubscription();
                        }
                    }
            },
            complete: () => {
                if(this.nextSlice) this.getRecordsByRecursion(page + 1);
                else{
                    this.reportService.setData(this.product+'-'+this.techs, {dataSource: this.dataSource, columns: this.columns});
                } 
            }  
        })
    }
}

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


import { Injectable } from '@angular/core';
import * as _ from 'lodash';
import { isNil } from 'lodash';
import { Constants } from 'src/constants/constants';
import {
    Cell,
    CellEmpty,
    CellStage,
    CellText,
    CellTypes,
    Column,
    ConditionalStageResponse,
    Job,
    MandatoryStage,
    Metric,
    Report,
    Row,
    Stage
} from 'src/models';
import { CellCustomStage } from '../models/cell/cell-custom-stage';
import { ColumnType } from '../models/column-type';

@Injectable({
    providedIn: 'root'
})
export class TableMapperService {
    private jobNameColumnId = 1;
    private buildNameColumnId = 2;
    private buildSuccPercColumnId = 3;
    private helmVersionColumnId = 4;
    private cbosVersionColumnId = 5;
    private sonarQubeColumnId = 6;
    private lastMetaColumnId = this.sonarQubeColumnId;
    private isCurrentCbos = false;
    private pipelineName = Constants.PIPELINE;
    private helmVersion = Constants.HELM_VERSION;
    private cBosVersion = Constants.CBOS_VERSION;
    private customStages = Constants.CUSTOM_STAGES;
    private buildNumber = Constants.BUILD_NUMBER;
    private notAvail = Constants.NOT_AVAILABLE;
    private lastSuccessBuildDuration: any;
    private lastSuccessBuildTimestamp: any;
    private isAligned : any;
    private helmVersionText = Constants.HELM_VERSION_TEXT;
    private cBosVersionText = Constants.CBOS_VERSION_TEXT;
    private buildNumberText = Constants.BUILD_NO_TEXT;
    private alignText = Constants.ALIGN_TEXT
    private pipelineText = Constants.MS_PIPELINE_TOOLTIP
    constructor() {
    }

    public getColumns(report: Report): Column[] {
        const columns: Column[] = [];
        columns.push(this.getNameColumn(this.jobNameColumnId, ColumnType.COLUMN, true, this.pipelineName, this.pipelineText));
        columns.push((this.getColumn(this.buildNameColumnId, this.buildNumber, ColumnType.COLUMN, true, [],this.buildNumberText)));
        columns.push(this.getColumn(this.buildSuccPercColumnId, 'Alignment(%)', ColumnType.COLUMN, true, [], this.alignText));
        columns.push(this.getColumn(this.helmVersionColumnId, this.helmVersion, ColumnType.COLUMN, true, [],this.helmVersionText));
        columns.push(this.getColumn(this.cbosVersionColumnId, this.cBosVersion, ColumnType.COLUMN, true, [],this.cBosVersionText));
        columns.push(this.getColumn(this.sonarQubeColumnId, Constants.SONAR_METRICS_COLUMN, ColumnType.COLUMN, true, [], Constants.SONAR_METRICS_TOOLTIP));
        if (report?.mandatoryStages?.length > 0) {
            columns.push(this.getMandatoryStagesColumns(report.mandatoryStages, columns.length));
        }
        columns.push(this.getColumn(128, 'column-spacer', ColumnType.SPACER, true))
        if (report?.maxUnknownStages > 0) {
            columns.push(this.getCustomStagesColumns(report.maxUnknownStages, report?.mandatoryStages?.length))
        }
        return columns;
    }

    public getRows(report: Report): Row[] {
        
        return report?.jobs?.map(job => this.getRow(job, report?.mandatoryStages));
    }

    public getRow(job: Job, mandatoryStages: MandatoryStage[]): Row {
        const cells: Cell[] = [];
        cells.push(this.getJobNameCell(job));
        cells.push(this.getBuildNameCell(job));
        cells.push(this.getBuildTrends(job));
        cells.push(this.getHelmVersionCell(job));
        cells.push(this.getCbosVersionCell(job));
        mandatoryStages.forEach((mandatoryStage, index) => {
            const stage = job?.mandatoryStages?.find(stage => stage.name === mandatoryStage.name);
            const conditionalStages = job.conditionalStages;
            const stageColumnId = this.getNextId(index + this.lastMetaColumnId);
            if (stage) {
                let cell = this.getCellStage(stageColumnId, stage, conditionalStages);
                if(stage.blueoceanStageInfo && stage.blueoceanStageInfo !== null) {
                    cell.skippedStage = stage.blueoceanStageInfo.state === 'SKIPPED';
                } 
                
                cells.push(cell);
            } else{
                cells.push(this.getCellMissingStage(stageColumnId, mandatoryStage.name));
            }
        });
        job.unknownStages?.forEach((stage, index) => {
            const stageColumnId = this.getNextId(mandatoryStages.length + this.lastMetaColumnId + index);
            cells.push(this.getCellCustomStage(stageColumnId, stage));
        })
        cells.push(this.getSonarQubeCell(job, cells));
        return {
            cells
        };
    }

    private getJobNameCell(job: Job): CellText {
        return this.getCellText(this.jobNameColumnId, job.jobName, job.jobUrl,job.jobproductList);
    }

    private getBuildNameCell(job: Job): CellText {
        this.lastSuccessBuildTimestamp = job.lastSuccessBuildTimestamp;
        this.lastSuccessBuildDuration = job.lastSuccessBuildDuration;
        return this.getCellText(this.buildNameColumnId, job.buildNo, job.buildUrl);
    }

    private getBuildTrends(job:Job): CellText {
        this.isAligned = job.stageAlignedOrNot;
        return this.getCellText(this.buildSuccPercColumnId, job.buildStagePercentage?.toString() ?? '0', '');
    }

    private getCbosVersionCell(job: Job): CellText {
        const value = isNil(job.cbosVersion) ? Constants.VERSION_NA : job.cbosVersion;
        this.isCurrentCbos = job.currentCbosVersion;
        return this.getCellText(this.cbosVersionColumnId, value, '');
    }
    private getSonarStageStatus(stage: Stage) : boolean {
        return stage?.coveredRules !== undefined && stage?.mandatoryRulesCount !== undefined && stage?.coveredRules > 0 && stage.coveredRules === stage.mandatoryRulesCount;
    }
    private getSonarQubeCell(job: Job, cells: Cell[]): CellText {
        let isSonarPublishPassed: boolean = false;
        let isSonarQualityGateStagePassed: boolean = false;
        let sonarMissing: boolean = false;

        cells.forEach(cell => {
            if('stageName' in cell){
                const stageCell = cell as CellStage;
                    if(stageCell.stageName === 'SonarQube'){
                    sonarMissing = stageCell.missing;
                }
            }
        })
        const sonarStages = ['SonarQube' , 'SonarQube Quality Gate' ];
        const sonarStageAndQualityGateStages = job?.mandatoryStages?.filter(stage => sonarStages.includes(stage.name));
        const len = sonarStageAndQualityGateStages?.length;

        sonarStageAndQualityGateStages?.forEach(stage => {
            const currentStageStatus = this.getSonarStageStatus(stage);
            switch(stage.name){
                case 'SonarQube Quality Gate':
                    isSonarQualityGateStagePassed = currentStageStatus;
                    break;
                case 'SonarQube':
                    isSonarPublishPassed = currentStageStatus;
                    break;
            }
        })

        var sonarStageStatus = 'FAILED';
        if(len === 2){
            sonarStageStatus = isSonarPublishPassed ? (isSonarQualityGateStagePassed ? 'SUCCESS' : 'PARTIAL' ) : 'FAILED';
        }else if(len === 1 && isSonarPublishPassed){
            sonarStageStatus = 'SUCCESS';
        }

        return this.getCellText(this.sonarQubeColumnId, '', '', [], job.sonarMetrics, job.sonarQubeUrl, job.sonarQualityGateStatus, sonarStageStatus, job.sonarReportStatusCode, sonarMissing);
    }
    private getHelmVersionCell(job: Job): CellText {
        const value = isNil(job.helmVersion) ? Constants.VERSION_NA : job.helmVersion;
        return this.getCellText(this.helmVersionColumnId, value, '');
    }

    private getCellText(id: number,
                        text: string,
                        url?: string,
                        jobproductList?: string[],
                        sonarMetrics?: Metric[],
                        sonarQubeUrl?: string,
                        sonarQualityGateStatus?: string,
                        sonarStageStatus?: string,
                        sonarReportStatusCode?: number,
                        sonarMissing?:boolean
                        ): CellText {
        return {
            id: id,
            columnId: id,
            text,
            isCurrentCbos: this.isCurrentCbos,
            lastSuccessBuildDuration: this.lastSuccessBuildDuration,
            lastSuccessBuildTimestamp: this.lastSuccessBuildTimestamp,
            isAligned: this.isAligned,
            url,
            jobproductList,
            sonarMetrics,
            sonarQubeUrl,
            sonarQualityGateStatus,
            sonarStageStatus,
            sonarReportStatusCode,
            sonarMissing,
            type: CellTypes.cellText
        }
    }

    private getCellCustomStage(id: number, stage: Stage): CellCustomStage {
        const cell = new CellCustomStage();
        cell.id = id;
        cell.columnId = id;
        cell.name = stage.name;
        cell.rules = stage.rules;
        return cell;
    }

    private getCellMissingStage(id: number, stageName: string): CellStage {
        const cell = new CellStage();
        cell.id = id;
        cell.columnId = id;
        cell.stageName = stageName;
        cell.missing = true;
        return cell;
    }

    private getCellStage(id: number, stage: Stage, conditionalStages: ConditionalStageResponse): CellStage {
        const cell = new CellStage();
        cell.id = id;
        cell.columnId = id;
        cell.stageName = stage.name;
        cell.coveredRules = stage.coveredRules;
        cell.mandatoryRulesCount = stage.mandatoryRulesCount;
        cell.missingRules = stage.missingRules;
        cell.missing = false;
        if(conditionalStages[stage.name]) {
            cell.conditionalStageStatus = conditionalStages[stage.name];
        }
        return cell;
    }

    private getMandatoryStagesColumns(stages: MandatoryStage[], columns: number): Column {
        const firstMandatoryId = this.getNextId(columns);
        const firstMandatoryIndex = 0;
        const firstMandatoryColumn = this.getStageColumn(stages[firstMandatoryIndex], firstMandatoryId, ColumnType.PARENT_COLUMN, true);
        firstMandatoryColumn.subColumns = stages.filter((stage, index) => index !== firstMandatoryIndex)
            .map((stage, index) => this.getStageColumn(stage, this.getNextId(index + firstMandatoryId), ColumnType.SUB_COLUMN, true));
        return firstMandatoryColumn;
    }

    private getCustomStagesColumns(maxCustomStages: number, mandatoryStagesLength: number = 0): Column {
        const firstCustomStageId = this.getNextId(this.lastMetaColumnId + mandatoryStagesLength);
        if (maxCustomStages === 1) {
            return this.getColumn(firstCustomStageId, this.customStages, ColumnType.COLUMN, false);
        } else {
            const firstCustomStageColumn = this.getColumn(firstCustomStageId, this.customStages, ColumnType.PARENT_COLUMN, false);
            firstCustomStageColumn.subColumns = _.range(1, maxCustomStages)
                .map(value => value + firstCustomStageId)
                .map(id => this.getNameColumn(id, ColumnType.SUB_COLUMN, false));
            return firstCustomStageColumn;
        }
    }

    private getNextId(lastId: number): number {
        return lastId + 1;
    }

    private getStageColumn(stage: MandatoryStage,
                           id: number,
                           type: ColumnType, isGolden: boolean): Column {
        return this.getColumn(id, stage.name, type, isGolden, stage.rules, undefined, stage.gatingEnabled);
    }

    private getNameColumn(id: number,
                          type: ColumnType,
                          isShown: boolean,
                        header?: string, headerTooltip?: string): Column {
        return this.getColumn(id, header ?? '', type, isShown, undefined, headerTooltip);
    }

    private getColumn(id: number,
                      header: string,
                      type: ColumnType,
                      isGolden: boolean,
                      rules?: string[], headerToolTip?: string, gatingEnabled = false): Column {
        return {
            id: `${id}`,
            columnDef: `${id}`,
            header,
            hide:  type === ColumnType.SUB_COLUMN && !isGolden,
            isExpanded: isGolden,
            gatingEnabled,
            type,
            rules,
            headerToolTip,
            cell: (cells: Array<Cell>) => this.findCell(cells, id)
        }
    }

    private findCell(cells: Array<Cell>, id: number): Cell {
        if (cells === null) {
            return new CellEmpty();
        }
        const cell = cells.find(s => s.columnId === id);
        return cell ? cell : new CellEmpty();
    }
}

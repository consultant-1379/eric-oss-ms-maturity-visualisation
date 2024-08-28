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



import { DecimalPipe } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { Constants } from 'src/constants/constants';
import { CellText } from 'src/models';

@Component({
  selector: 'cell-header',
  templateUrl: './cell-header.component.html',
  styleUrls: ['./cell-header.component.scss'],
  providers: [DecimalPipe]
})
export class CellHeaderComponent implements OnInit {
  @Input() cell!: CellText;
  notAvail: string = Constants.NOT_AVAILABLE;
  unAvailable: string = Constants.VERSION_NA;
  notAvailSonar: string = Constants.NOT_AVAILABLE_SONAR;
  duration: string = Constants.LAST_SUCCESS_BUILD_DURATION;
  lastRunDate: string = Constants.LAST_RUN_DATE;
  bugs: string = Constants.BUGS;
  vulnerabilities: string = Constants.VULNERABILITIES;
  coverage: string = Constants.COVERAGE;
  sqale_index: string = Constants.SQALE_INDEX;
  buildDuration: any;
  buildTimestamp: any;
  sonarValues: { [x: string]: string } = {
    new_bugs: 'NA',
    new_vulnerabilities: 'NA',
    new_coverage: '0%',
    new_technical_debt: 'NA'
  }

  constructor(private decimalPipe: DecimalPipe) { }

  ngOnInit(): void {
    this.buildDuration = this.cell.lastSuccessBuildDuration;
    this.buildTimestamp = this.cell.lastSuccessBuildTimestamp;
    if(this.cell.id === 6){
      this.cell.sonarMetrics?.map(sonarMetric => {
        if (sonarMetric.metric === this.coverage) {
          this.sonarValues[this.coverage] = this.decimalPipe.transform(sonarMetric.value, '1.0-2') + '%' ;
        }
        else if (sonarMetric.metric == this.sqale_index) {
          const minutes = parseInt(sonarMetric.value, 10);
          const formattedDays = Math.floor(minutes / (60 * 8));
          const formattedHours = Math.floor((minutes % (60 * 8)) / 60);
          const formattedMins = minutes % 60;
          this.sonarValues[sonarMetric.metric] = `${formattedDays === 0 ? '' : formattedDays + 'd '}${formattedHours === 0 ? '' : formattedHours + 'h '}${formattedMins}m`
        }
        else this.sonarValues[sonarMetric.metric] = sonarMetric.value;
      })
    }

  }

  getSonarMetricsTooltip(metric: string): string {
    const value = this.sonarValues[metric] ;
    switch(metric) {
      case this.bugs:
        return `Bugs : ${value}` ;
      case this.vulnerabilities:
        return `Vulnerabilities : ${value}`;
      case this.coverage:
        return `Coverage : ${value}`;
      case this.sqale_index:
        return `Code Debt : ${value}`;
      default: return 'NA'
    }
  }
  getSonarFailureTooltip(): string {
    if(this.cell.sonarStageStatus === 'SUCCESS' || this.cell.sonarStageStatus === 'PARTIAL'){
      switch(this.cell.sonarReportStatusCode){
        case 404:
          return Constants.SONAR_METRICS_NA_MESSAGE;
        case 403:
          return 'Not authorized to access this report';
        case 200:
          return 'Measures on New Code will appear after the second analysis of this branch.';
        default: return 'The microsevice might have been obsoleted'
      }
    }

    else if(this.cell.sonarMissing){
      return 'The SonarQube stage is skipped in the pipeline'
    }
    return 'The SonarQube stage has not passed all the rules'
  }

  getSonarStatusCodeText(): string{
    switch(this.cell.sonarReportStatusCode){
      case 404:
        return 'NOT FOUND';
      case 403:
        return 'NO ACCESS';
      case 200:
        return 'NO NEW CODE';
      default: return 'NA';
    }
  }
  getBuildTooltip(): string {
    return `${this.duration} ${this.buildDuration} \n ${this.lastRunDate} ${this.buildTimestamp}`;
  }
  getJobTooltip(): string {
    var toolTip = '';
    if (this.cell.jobproductList) {
      for (let val of this.cell.jobproductList) {
        toolTip += toolTip?', ' + val:val;
      }
    }
    return 'Common job in " ' + toolTip + ' " applications';
  }
}

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


import { ConditionalStageStatus } from './cell/cell-stage';
import { Stage } from './stage';

export class Job {
  [x: string]: any;
  jobName!: string;
  jobUrl!: string;
  buildNo!: string;
  buildUrl!: string;
  stageAlignedOrNot!: boolean;
  helmVersion?: string
  cbosVersion?: string;
  currentCbosVersion!: boolean;
  mandatoryStages?: Stage[];
  unknownStages?: Stage[];
  skippedStages?: Stage[];
  lastSuccessBuildDuration!: string;
  lastSuccessBuildTimestamp!: string;
  buildStagePercentage!: string;
  jobproductList?: string[];
  sonarMetrics?:Metric[];
  sonarQubeUrl?: string;
  sonarQualityGateStatus?: string;
  sonarReportStatusCode?: number;
  conditionalStages: ConditionalStageResponse = {};
}

export type ConditionalStageResponse =  {[key: string] : ConditionalStageStatus };

export class Metric{
  metric!: string;
  value!: string;
  period!: any;
}
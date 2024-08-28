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



import {CellTypes} from './cell-type';
import {Cell} from './cell';
import { Metric } from '../job';

export class CellText implements Cell{
  id!: number;
  columnId!: number;
  text!: string;
  isCurrentCbos!: boolean;
  lastSuccessBuildDuration!: string;
  lastSuccessBuildTimestamp!: string;
  isAligned!: boolean;
  url?: string;
  type: CellTypes = CellTypes.cellText;
  jobproductList?: string[];
  sonarMetrics?: Metric[];
  sonarQubeUrl?: string;
  sonarQualityGateStatus?: string;
  sonarStageStatus?: string;
  sonarReportStatusCode?: number;
  sonarMissing?:boolean;
}

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

import { isEmpty } from "lodash";
import { Cell } from "./cell";
import { CellTypes } from "./cell-type";
import { CellWithRule } from "./cell-with-rule";

export class CellStage implements Cell, CellWithRule {
  id!: number;
  columnId!: number;
  stageName!: string;
  coveredRules?: number;
  mandatoryRulesCount?: number;
  skippedStage?: boolean;
  missing!: boolean;
  rules?: string[];
  missingRules?: string[];
  type: CellTypes = CellTypes.cellStage;
  conditionalStageStatus?: ConditionalStageStatus;
  blueoceanStageInfo!: BlueoceanStageInfo | null;

  hasRules(): boolean {
    return !isEmpty(this.missingRules);
  }
}

export type ConditionalStageStatus = "SUCCESS" | "FAILURE" | "PARTIAL";

export type BlueoceanStageInfo = {
  displayName: string;
  durationInMillis: number;
  type: StageType;
  result: StageResult;
  state: StageState;
};

export type StageType = "STAGE" | "PARALLEL";

export type StageResult = "SUCCESS" | "NOT_BUILT" | "UNSTABLE" | "FAILURE";

export type StageState = "FINISHED" | "SKIPPED" | "NOT_BUILT";

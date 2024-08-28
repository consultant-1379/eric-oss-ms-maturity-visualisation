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


import {Job} from './job';
import {MandatoryStage} from './mandatory-stage';

export class Report {
  appType!: string;
  jobType!: string;
  mandatoryStages!: MandatoryStage[];
  jobs: Job[] = [];
  maxUnknownStages!: number;
}

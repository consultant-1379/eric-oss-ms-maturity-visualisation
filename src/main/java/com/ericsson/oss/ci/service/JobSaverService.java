/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.ci.service;

import java.util.List;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.entity.BlueoceanStageEntity;
import com.ericsson.oss.ci.domain.entity.JobEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.JobWithDetails;

public interface JobSaverService {
    void saveBuild(String jenkinsUrl, JobWithDetails jenkinsJob, Build jenkinsBuild, ParseResult parseResult, Job job);
    String getSonarMetrics(final JobTypeEnum jobType, final String sonarUrl, final String buildStatus);
    List<BlueoceanStageEntity> getBlueoceanStagesInfo(String jenkinsUrl, String jobName, int buildNo);
    JobEntity getJobEntity(final String jenkinsUrl, final JobWithDetails jenkinsJob, final Job job);
}

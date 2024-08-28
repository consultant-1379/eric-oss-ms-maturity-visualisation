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
package com.ericsson.oss.ci.service.impl;

import static com.ericsson.oss.ci.service.impl.TestHelper.getJobEntity;
import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomLong;
import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomStageEntity;
import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomString;
import static com.ericsson.oss.ci.service.impl.TestHelper.getStage;
import static com.ericsson.oss.ci.service.impl.TestHelper.getStageEntity;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.ci.domain.dto.configuration.Stage;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.entity.BlueoceanStageEntity;
import com.ericsson.oss.ci.domain.entity.BuildEntity;
import com.ericsson.oss.ci.domain.entity.JobEntity;
import com.ericsson.oss.ci.domain.entity.StageEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.ericsson.oss.ci.domain.repository.BlueoceanStageRepository;
import com.ericsson.oss.ci.domain.repository.BuildRepository;
import com.ericsson.oss.ci.domain.repository.JobRepository;
import com.ericsson.oss.ci.jenkins.JobTypeMatcher;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.JobDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportListDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportsDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.StageDto;

class ReportMapperHelperTest {
	private BuildRepository buildRepository;
	private JobRepository jobRepository;
	private BlueoceanStageRepository blueoceanStageRepository;

    @Test
    void mapEmptyReports() {
        final ReportMapperHelper helper = getReportMapperHelper();
        final ReportsDto reportsDto = helper.map(Boolean.FALSE,emptyList());
        ReportDto reports = reportsDto.getReports();
        Assertions.assertNull(reports);
        assertTrue(reportsDto.getUnknownJobNames().isEmpty());
    }

    @Test
    void getUnknownJobNamesWithAppTypeTest() {
        final Stages stages = TestHelper.getStages(List.of(
                TestHelper.getStandard(getRandomString(), emptyList(), emptyList())
        ));
        final String jobName = getRandomString();
        final String appType = getRandomString();

        final ReportMapperHelper helper = getReportMapperHelper(stages);
        final ReportsDto reportsDto = helper.map(Boolean.FALSE,List.of(Pair.of(getJobEntity(jobName, appType), new BuildEntity())));

        ReportDto reports = reportsDto.getReports();
        Assertions.assertNull(reports);
        assertEquals(1, reportsDto.getUnknownJobNames().size());

        final String expectedUnknownJobName = jobName + " " + appType;
        assertEquals(expectedUnknownJobName, reportsDto.getUnknownJobNames().get(0));
    }
    @Test
    void mapTest(){
        final String appType = getRandomString();
        final String rule = getRandomString();
        final Stage stage = getStage(Set.of(rule));
        final Stage sonarStage = getStage("SonarQube", new HashSet<>(Arrays.asList("x-pcr", "x-release")));
        final Stage standardConditionalStage = getStage("API NBC Check", new HashSet<>(Arrays.asList("Bob Rule: rest-2-html:check-if-openapispecfile-present", "x-release")));
        final Stages stages = TestHelper.getStages(List.of(
                TestHelper.getStandard(appType, List.of(stage,sonarStage, standardConditionalStage), List.of(stage,sonarStage))
        ));
        final JobEntity jobEntity = getJobEntity(getRandomString(), appType);
        final StageEntity mandatoryStage = getStageEntity(stage.getName(), stage.getRules());
        final StageEntity customStages = getStageEntity("CUSTOM_STAGES", stage.getRules());
        final StageEntity skippedStages = getStageEntity("SKIPPED_STAGES", stage.getRules());
        final StageEntity conditionalStage = getStageEntity("API NBC Check", new HashSet<>(Arrays.asList("Bob Rule: rest-2-html:check-if-openapispecfile-present", "")));
        final StageEntity unknownStage = getRandomStageEntity();
        final Double buildAlignmentPerc = (double)getRandomLong();
        final Integer buildAlignedOrNot = 0;
        final String buildAlignmentValue = buildAlignmentPerc + ";" + buildAlignedOrNot;
        final String vaAlignmentValue = buildAlignmentPerc + ";0";
        final String testTask = "TestTask";
        final String testAlignString = testTask + ":" + buildAlignmentValue + "\n";
        Map<String, Pair<Double, Integer>> result = ReportMapperHelper.getVAalignmentResult(testAlignString);
        assert(result.containsKey(testTask));
        Pair<Double, Integer> alignmentPair = result.get(testTask);
        assert(alignmentPair.getLeft().equals(buildAlignmentPerc));
        assert(alignmentPair.getRight().equals(buildAlignedOrNot));

        final BuildEntity buildEntity = getBuildEntity();
        buildEntity.setStages(List.of(mandatoryStage, unknownStage, customStages,skippedStages, conditionalStage));
        buildEntity.setSonarMetrics(";new_bugs:1;quality_gate_details:OK;statusCode:200");
        buildEntity.setBuildAlignment(buildAlignmentValue);
        buildEntity.setVaAlignment(vaAlignmentValue);
        final String jobTypeLabel = getRandomString();
        final ReportMapperHelper helper = getReportMapperHelper(stages, jobTypeLabel);

        when(jobRepository.findProductsByJob(jobEntity.getId())).thenReturn(List.of(getRandomString()));
        when(blueoceanStageRepository.findByBuild(buildEntity)).thenReturn(Collections.emptyList() );
        final ReportsDto reportsDto = helper.map(Boolean.FALSE,List.of(Pair.of(jobEntity, buildEntity)));
        jobEntity.setType(JobTypeEnum.PRE_CODE_REVIEW);

        assertTrue(reportsDto.getUnknownJobNames().isEmpty());
        ReportDto reports = reportsDto.getReports();
        assertTrue(reports != null);
        final ReportDto report = reportsDto.getReports();
        checkReports(report, jobEntity, jobTypeLabel);
        assertEquals(1, report.getJobs().size());
        final JobDto jobDto = report.getJobs().get(0);
        checkJob(jobDto, jobEntity, buildEntity);
        checkMandatoryStage(jobDto, stage);
        checkUnknownStage(jobDto, unknownStage);
        assertFalse(jobDto.getStageAlignedOrNot());
        assertEquals(buildAlignmentPerc, jobDto.getBuildStagePercentage());
        assertEquals("OK", jobDto.getSonarQualityGateStatus());
        assertEquals(200, jobDto.getSonarReportStatusCode());
        assertEquals(1, jobDto.getSonarMetrics().size());
    }
    
    @Test
    void mapsTest(){
        final String appType = getRandomString();
        final String rule = getRandomString();
        final Stage stage = getStage(Set.of(rule));
        final Stage sonarStage = getStage("SonarQube", new HashSet<>(Arrays.asList("x-pcr", "x-release")));
        final Stage standardConditionalStage = getStage("API NBC Check", new HashSet<>(Arrays.asList("Bob Rule: rest-2-html:check-if-openapispecfile-present", "x-release")));
        final Stages stages = TestHelper.getStages(List.of(
                TestHelper.getStandard(appType, List.of(stage,sonarStage, standardConditionalStage), List.of(stage,sonarStage))
        ));
        final JobEntity jobEntity = getJobEntity(getRandomString(), appType);
        final StageEntity mandatoryStage = getStageEntity(stage.getName(), stage.getRules());
        final StageEntity customStages = getStageEntity("CUSTOM_STAGES", stage.getRules());
        final StageEntity skippedStages = getStageEntity("SKIPPED_STAGES", stage.getRules());
        final StageEntity conditionalStage = getStageEntity("API NBC Check", new HashSet<>(Arrays.asList("Bob Rule: rest-2-html:check-if-openapispecfile-present", "")));
        final StageEntity unknownStage = getRandomStageEntity();
        final Double buildAlignmentPerc = (double)getRandomLong();
        final Boolean buildAlignedOrNot = null;
        final String buildAlignmentValue = buildAlignmentPerc + ";" + buildAlignedOrNot;
        final String vaAlignmentValue = buildAlignmentPerc + ";0";
        final BuildEntity buildEntity = getBuildEntity();
        buildEntity.setStages(List.of(mandatoryStage, unknownStage, customStages,skippedStages, conditionalStage));
        buildEntity.setSonarMetrics(";new_bugs:1;quality_gate_details:OK;statusCode:200");
        buildEntity.setBuildAlignment(buildAlignmentValue);
        buildEntity.setVaAlignment(vaAlignmentValue);
        final String jobTypeLabel = getRandomString();
        
        BlueoceanStageEntity b = new BlueoceanStageEntity();
        b.setDisplayName("API NBC Check");

        final ReportMapperHelper helper = getReportMapperHelper(stages, jobTypeLabel);
        when(jobRepository.findProductsByJob(jobEntity.getId())).thenReturn(List.of(getRandomString()));
        when(blueoceanStageRepository.findByBuild(buildEntity)).thenReturn(List.of(b));
        final ReportListDto reportsDto = helper.maps(Boolean.FALSE,List.of(Pair.of(jobEntity, buildEntity)));
        jobEntity.setType(JobTypeEnum.PRE_CODE_REVIEW);

        assertTrue(reportsDto.getUnknownJobNames().isEmpty());
        ReportDto reports = reportsDto.getReports().get(0);
        assertTrue(reports != null);
        final ReportDto report = reportsDto.getReports().get(0);
        checkReports(report, jobEntity, jobTypeLabel);
        assertEquals(1, report.getJobs().size());
        final JobDto jobDto = report.getJobs().get(0);
        checkJob(jobDto, jobEntity, buildEntity);
        checkMandatoryStage(jobDto, stage);
        checkUnknownStage(jobDto, unknownStage);
        assertNull(jobDto.getStageAlignedOrNot());
        assertEquals(buildAlignmentPerc, jobDto.getBuildStagePercentage());
        assertEquals("OK", jobDto.getSonarQualityGateStatus());
        assertEquals(200, jobDto.getSonarReportStatusCode());
        assertEquals(1, jobDto.getSonarMetrics().size());
    }

    private void checkUnknownStage(final JobDto jobDto, final StageEntity unknownStage) {
        assertEquals(2, jobDto.getUnknownStages().size());
        final StageDto stageDto = jobDto.getUnknownStages().iterator().next();
        assertEquals(stageDto.getName(), unknownStage.getName());
        assertTrue(CollectionUtils.isEqualCollection(stageDto.getRules(), unknownStage.getRules()));
        assertEquals(stageDto.getUnknownRules(), unknownStage.getRules().size());
    }

    private void checkMandatoryStage(final JobDto jobDto,
                                     final Stage stage) {
        assertEquals(2, jobDto.getMandatoryStages().size());
        final StageDto stageDto = jobDto.getMandatoryStages().iterator().next();
        assertTrue(CollectionUtils.isEqualCollection(stageDto.getRules(), stage.getRules()));
        assertEquals(stageDto.getName(), stage.getName());
        assertEquals(stageDto.getMandatoryRulesCount(), stage.getRules().size());
        assertEquals(stageDto.getCoveredRules(), stage.getRules().size());
        assertEquals(0 ,stageDto.getUnknownRules());
    }

    private void checkJob(final JobDto jobDto,
                          final JobEntity jobEntity,
                          final BuildEntity buildEntity) {
        assertEquals(jobDto.getJobName(), jobEntity.getName());
        assertEquals(jobDto.getJobUrl(), jobEntity.getUrl());
        assertEquals(jobDto.getBuildNo(), String.valueOf(buildEntity.getBuildNo()));
        assertEquals(jobDto.getBuildUrl(), buildEntity.getUrl());
    }

    private void checkReports(final ReportDto report,
                              final JobEntity jobEntity,
                              final String jobTypeLabel) {
        assertEquals(jobEntity.getAppType(), report.getAppType());
        assertEquals(jobTypeLabel, report.getJobType());
    }
    
    private BuildEntity getBuildEntity() {
        final BuildEntity buildEntity = new BuildEntity();
        buildEntity.setUrl(getRandomString());
        return buildEntity;
    }

    private ReportMapperHelper getReportMapperHelper() {
        return getReportMapperHelper(new Stages());
    }

    private ReportMapperHelper getReportMapperHelper(final Stages stages) {
        return new ReportMapperHelper(stages, mock(JobTypeMatcher.class), mock(BuildRepository.class),mock(JobRepository.class), mock(BlueoceanStageRepository.class));
    }

    private ReportMapperHelper getReportMapperHelper(final Stages stages, final String jobTypeLabel) {
    	buildRepository=mock(BuildRepository.class);
    	jobRepository=mock(JobRepository.class);
    	blueoceanStageRepository=mock(BlueoceanStageRepository.class);
        final JobTypeMatcher jobTypeMatcher = mock(JobTypeMatcher.class);
        when(jobTypeMatcher.getJobTypeLabel(any())).thenReturn(jobTypeLabel);
        return new ReportMapperHelper(stages, jobTypeMatcher,buildRepository,jobRepository, blueoceanStageRepository);
    }
}

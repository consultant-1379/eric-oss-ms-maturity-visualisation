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

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.select;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.collections4.ListUtils.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

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
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.BlueoceanStageDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ConditionalExecStatus;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.JobDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.MandatoryStageDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.MetricDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportListDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportsDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.StageDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportMapperHelper {
    private final Set<String> appTypes;
    private String maxVersion=null;
    private final BuildRepository buildRepository;
    private final JobRepository jobRepository;
    private final BlueoceanStageRepository blueoceanStageRepository;
    private final Map<String, Map<JobTypeEnum, List<Stage>>> stagesByAppTypeAndJobType;
    private final JobTypeMatcher jobTypeMatcher;
    private final Set<String> conditionalStagesSet = Set.of("API NBC Check");
    private final Set<String> gatingEnabledStages = Set.of("API NBC Check");
    private final Map<String, ConditionalExecStatus> conditionalRulesForAPINBCCheck = Map.ofEntries(
    		Map.entry("Bob Rule: rest-2-html", ConditionalExecStatus.PARTIAL),
    		Map.entry("Bob Task: rest-2-html:check-if-openapispecfile-present", ConditionalExecStatus.PARTIAL),
    		Map.entry("Bob Task: rest-2-html:count-apiversions", ConditionalExecStatus.SUCCESS),
    		Map.entry("Bob Task: rest-2-html:check-has-open-api-been-modified", ConditionalExecStatus.SUCCESS),
    		Map.entry("Bob Task: rest-2-html:zip-open-api-doc", ConditionalExecStatus.PARTIAL),
    		Map.entry("Bob Task: rest-2-html:generate-html-output-files", ConditionalExecStatus.SUCCESS)
    	);

    public ReportMapperHelper(final Stages stages,final JobTypeMatcher jobTypeMatcher, BuildRepository buildRepository,
    		JobRepository jobRepository, final BlueoceanStageRepository blueoceanStageRepository) {
        this.jobTypeMatcher = jobTypeMatcher;
        this.buildRepository=buildRepository;
        this.jobRepository=jobRepository;
        this.blueoceanStageRepository = blueoceanStageRepository;
        final List<Stages.Standard> standards = emptyIfNull(stages.getStandards());
        stagesByAppTypeAndJobType = standards.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Stages.Standard::getAppType,
                        standard -> Map.of(
                                JobTypeEnum.PUBLISH, unmodifiableList(standard.getPublish()),
                                JobTypeEnum.PRE_CODE_REVIEW, unmodifiableList(standard.getPreCodeReview())
                )));
        appTypes = emptyIfNull(stages.getStandards()).stream()
                .map(Stages.Standard::getAppType)
                .collect(Collectors.toUnmodifiableSet());
    }

    ReportsDto map(final Boolean nextSlice, final List<Pair<JobEntity, BuildEntity>> jobsWithBuilds) {
        final Map<String, Map<JobTypeEnum, List<Pair<JobEntity, BuildEntity>>>> jobsByAppTypeAndJobType = getJobsByAppTypeAndJobType(jobsWithBuilds);
        ReportDto reports = null;
        maxVersion = buildRepository.getMaxVersion();
        for (Map.Entry<String, Map<JobTypeEnum, List<Pair<JobEntity, BuildEntity>>>> appTypeEntry : jobsByAppTypeAndJobType.entrySet()) {
            for (Map.Entry<JobTypeEnum, List<Pair<JobEntity, BuildEntity>>> jobByTypeEntry : appTypeEntry.getValue().entrySet()) {
                reports = mapReportDto(appTypeEntry.getKey(), jobByTypeEntry.getKey(), jobByTypeEntry.getValue());
            }
        }
        maxVersion=null;
        return ReportsDto.builder()
                .reports(reports)
                .unknownJobNames(getUnknownJobNamesWithAppType(jobsWithBuilds))
                .nextSlice(nextSlice)
                .build();
    }
    ReportListDto maps(final Boolean nextSlice, final List<Pair<JobEntity, BuildEntity>> jobsWithBuilds) {
        final Map<String, Map<JobTypeEnum, List<Pair<JobEntity, BuildEntity>>>> jobsByAppTypeAndJobType = getJobsByAppTypeAndJobType(jobsWithBuilds);
        List<ReportDto> rss=new ArrayList<>();
        maxVersion = buildRepository.getMaxVersion();
        for (Map.Entry<String, Map<JobTypeEnum, List<Pair<JobEntity, BuildEntity>>>> appTypeEntry : jobsByAppTypeAndJobType.entrySet()) {
            for (Map.Entry<JobTypeEnum, List<Pair<JobEntity, BuildEntity>>> jobByTypeEntry : appTypeEntry.getValue().entrySet()) {
               rss.add(mapReportDto(appTypeEntry.getKey(), jobByTypeEntry.getKey(), jobByTypeEntry.getValue()));
            }
        }
        return ReportListDto.builder()
                .reports(rss)
                .unknownJobNames(getUnknownJobNamesWithAppType(jobsWithBuilds))
                .nextSlice(nextSlice)
                .build();
    }

    private Map<String, Map<JobTypeEnum, List<Pair<JobEntity, BuildEntity>>>> getJobsByAppTypeAndJobType(final List<Pair<JobEntity, BuildEntity>> jobsWithBuilds) {
        return jobsWithBuilds.stream()
                .filter(pair -> appTypes.contains(pair.getKey().getAppType()))
                .collect(Collectors.groupingBy(
                        pair -> pair.getKey().getAppType(),
                        Collectors.groupingBy(pair -> pair.getKey().getType())
                ));
    }

    private List<String> getUnknownJobNamesWithAppType(final List<Pair<JobEntity, BuildEntity>> jobsWithBuilds) {
        return jobsWithBuilds.stream()
                .filter(pair -> !appTypes.contains(pair.getKey().getAppType()))
                .map(Pair::getKey)
                .map(this::getUnknownJobsNamesWithAppTypes)
                .collect(Collectors.toList());
    }

    private ReportDto mapReportDto(final String appType,
                                   final JobTypeEnum jobType,
                                   final List<Pair<JobEntity, BuildEntity>> jobWithBuilds) {
        final Map<String, Set<String>> stagesWithRules = getStagesWithRules(appType, jobType);
        final List<MandatoryStageDto> mandatoryStages = getMandatoryStages(appType, jobType);
        List<JobDto> jobs = null;
        ForkJoinPool customThreadPool = new ForkJoinPool(15);
        try {
			jobs = (customThreadPool.submit(() -> jobWithBuilds.parallelStream()
			        .map(pair -> mapToJobDto(pair, stagesWithRules, mandatoryStages))
			        .collect(Collectors.toList()))).get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("mapReportDto {} {} ",e);
		} finally {
		    customThreadPool.shutdown();
		}
        Map<String, Double> totalVaStagePercentages = new HashMap<>();
        Map<String, Integer> stageDiffs = new HashMap<>();
        Map<String, Double> averageVaStagePercentages = new HashMap<>();
        long unknownCount = 0;   

        if(jobs != null) {
            jobs.forEach(v->
        	  v.setCurrentCbosVersion(maxVersion!=null ? maxVersion.equals(v.getCbosVersion()):Boolean.FALSE)
            );

	    	final List<JobDto> finalJobs = jobs;
	        totalVaStagePercentages = jobs.stream()
	            .filter(job -> job != null && job.getMandatoryStages() != null)
	            .flatMap(job -> job.getMandatoryStages().stream())
	            .filter(stage -> stage != null && stage.getName() != null && stage.getStagePercentage() != null)
	            .collect(Collectors.groupingBy(StageDto::getName, 
	                    Collectors.summingDouble(StageDto::getStagePercentage)));

	        totalVaStagePercentages.forEach((stage, totalPercentage) -> 
	            averageVaStagePercentages.put(stage, totalPercentage / finalJobs.size()));
	        stageDiffs = jobs.stream()
	            .filter(job -> job != null && job.getMandatoryStages() != null)
	            .flatMap(job -> job.getMandatoryStages().stream())
	            .filter(stage -> stage != null && stage.getName() != null && stage.getStageDiff() != null)
	            .collect(Collectors.groupingBy(StageDto::getName, 
	                    Collectors.summingInt(StageDto::getStageDiff)));
	        unknownCount = getMaxUnknownStages(jobs);
        }
        return ReportDto.builder()
                .appType(appType)
                .jobType(jobTypeMatcher.getJobTypeLabel(jobType))
                .jobs(jobs)
                .mandatoryStages(mandatoryStages)
                .maxUnknownStages(unknownCount)
                .isStageAligned(stageDiffs)
                .totalStagePercentage(averageVaStagePercentages)
                .build();
    }
    private List<MandatoryStageDto> getMandatoryStages(final String appType, final JobTypeEnum jobType) {
        return Optional.of(appType)
                .map(stagesByAppTypeAndJobType::get)
                .map(map -> map.get(jobType))
                .orElseThrow(() -> new RuntimeException("Mandatory stages not found"))
                .stream()
                .map(stage ->{
                     Set<String> rules = stage.getRules();
                     if(stage.getName().equals("SonarQube")){
                           if (jobType.equals(JobTypeEnum.PRE_CODE_REVIEW)) {
                               rules.removeIf(rule -> rule.contains("release"));
                           }else {
                               rules.removeIf(rule -> rule.contains("pcr"));
                           }
                      }
                     Boolean gatingEnabled = gatingEnabledStages.contains(stage.getName());
                      return MandatoryStageDto.builder()
                        .name(stage.getName())
                        .rules(unmodifiableSet(rules))
                        .gatingEnabled(gatingEnabled)
                        .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, Set<String>> getStagesWithRules(final String appType, final JobTypeEnum jobType) {
        return Optional.of(appType)
                .map(stagesByAppTypeAndJobType::get)
                .map(map -> map.get(jobType))
                .orElseThrow(() -> new RuntimeException("Not found mandatory stages for " + appType + " " + jobType)).stream()
                .collect(Collectors.toUnmodifiableMap(Stage::getName, Stage::getRules));
    }

    private JobDto mapToJobDto(final Pair<JobEntity, BuildEntity> jobWithBuild,
                               final Map<String, Set<String>> stagesWithRules, List<MandatoryStageDto> mandatoryStages) {
    	log.info("mapToJobDto start {} {}");
    	final JobEntity jobEntity = jobWithBuild.getKey();
        final JobDto jobDto = new JobDto();

        final BuildEntity buildEntity = jobWithBuild.getValue();
        List<String> findProductByJob = jobRepository.findProductsByJob(jobEntity.getId());
        jobDto.jobproductList(findProductByJob);
        jobDto.setJobName(jobEntity.getName());
        jobDto.setJobUrl(jobEntity.getUrl());
        jobDto.setBuildNo(String.valueOf(buildEntity.getBuildNo()));
        jobDto.setBuildUrl(buildEntity.getUrl());
        jobDto.setBuildStatus(buildEntity.getBuildStatus());
        jobDto.setHelmVersion(buildEntity.getHelmVersion());
        jobDto.setCbosVersion(buildEntity.getCbosVersion());
        jobDto.setLastSuccessBuildDuration(buildEntity.getBuildDuration());
        jobDto.setLastSuccessBuildTimestamp(buildEntity.getBuildCreatedDt());
        Map<String, ConditionalExecStatus> conditionalStages = new HashMap<>();
        Map<String, Pair<Double, Integer>> vaAlignment =  getVAalignmentResult(buildEntity.getVaAlignment());
        emptyIfNull(buildEntity.getStages()).forEach(stage -> addStageDtoToJobDto(stage, jobDto, stagesWithRules,vaAlignment, conditionalStages, mandatoryStages));
        
        List<BlueoceanStageEntity> blueoceanStageEntities = blueoceanStageRepository.findByBuild(buildEntity);
        Set<StageDto> buildSpecificMandatoryStages = jobDto.getMandatoryStages();
        if(buildSpecificMandatoryStages != null) {
            buildSpecificMandatoryStages.forEach(stage -> blueoceanStageEntities.stream()
                	.filter(s -> s.getDisplayName().equalsIgnoreCase(stage.getName()))
                	.findFirst()
                	.ifPresent(s -> {
                		BlueoceanStageDto blueOceanStage = BlueoceanStageDto.builder()
                				.displayName(s.getDisplayName())
                				.durationInMillis(s.getDurationInMillis())
                				.result(s.getResult())
                				.state(s.getState())
                				.type(s.getType())
                				.build();
                		stage.setBlueoceanStageInfo(blueOceanStage);
                	})
                ); 	
        }
        
        jobDto.conditionalStages(conditionalStages);
        jobDto.setSonarQubeUrl(buildEntity.getSonarUrl());

		Pair<Double, Boolean> buildAlignment =  getBuildAlignmentResult(buildEntity.getBuildAlignment());
		

        jobDto.setBuildStagePercentage(buildAlignment.getLeft());
        jobDto.setStageAlignedOrNot(buildAlignment.getRight());

		String sonarMetricsString = buildEntity.getSonarMetrics();
        Set<MetricDto> sonarMetrics =  Arrays.stream(emptyStringIfNull(sonarMetricsString).split(";"))
                .filter(pair -> !pair.isEmpty())
                .map(pair -> {
                    String[] parts = pair.split(":");
                    if(parts.length == 2) {
                        String metric = parts[0];
                        String value = parts[1];
                        if (metric.equals("statusCode")) {
                            jobDto.setSonarReportStatusCode(Integer.valueOf(value));
                        } else if (metric.equals("quality_gate_details")) {
                            jobDto.setSonarQualityGateStatus(value);
                        } else {
                            MetricDto newMetric = new MetricDto();
                            newMetric.setMetric(metric);
                            newMetric.setValue(value);
                            return newMetric;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        jobDto.setSonarMetrics(sonarMetrics);
        log.info("mapToJobDto end {} {}");
        return jobDto;
    }


	private void addToUnknownStages(String stageName, JobDto jobDto) {
		StageDto customStage = new StageDto();
		customStage.setName(stageName);
		customStage.setRules(Collections.emptySet());
		jobDto.addUnknownStagesItem(customStage);
	}
	private void addToSkippedStages(String stageName, JobDto jobDto) {
		StageDto skippedstage = new StageDto();
		skippedstage.setName(stageName);
		skippedstage.setRules(Collections.emptySet());
		jobDto.addSkippedStagesItem(skippedstage);
	}

    private void addStageDtoToJobDto(final StageEntity stage,
                                     final JobDto jobDto,
                                     final Map<String, Set<String>> stagesWithRules,
                                     Map<String, Pair<Double, Integer>> vaAlignment, 
                                     Map<String, ConditionalExecStatus> conditionalStages, List<MandatoryStageDto> mandatoryStages) {

        if(stage.getName().equals("CUSTOM_STAGES")) {
            Optional.ofNullable(stage.getRules())
                    .ifPresent(rules -> rules.forEach(customStage -> addToUnknownStages(customStage, jobDto)));
            return;
         }
        if(stage.getName().equals("SKIPPED_STAGES")) {
            Optional.ofNullable(stage.getRules())
                    .ifPresent(rules -> rules.forEach(skippedstage -> addToSkippedStages(skippedstage, jobDto)));
            return;
         }
        
        if(conditionalStagesSet.contains(stage.getName())) {
        	String stageName = stage.getName();
        	if(stageName.equals("API NBC Check")) {
        		checkStatusBasedonRulesExecutionOrder(stageName, stage, conditionalStages, mandatoryStages);
        	}
        }
        

        final StageDto dto = new StageDto();
        dto.setName(stage.getName());
        dto.setRules(SetUtils.emptyIfNull(stage.getRules()));
        final Set<String> rules = stagesWithRules.get(dto.getName());
        if (rules != null) {
            dto.setMandatoryRulesCount(rules.size());
            final Map<Boolean, Set<String>> mandatoryWithUnknownRules = CollectionUtils.emptyIfNull(stage.getRules()).stream()
                    .collect(Collectors.partitioningBy(rules::contains, toSet()));
            final Set<String> coveredRules = mandatoryWithUnknownRules.get(true);
            dto.setCoveredRules(coveredRules.size());
            dto.setUnknownRules(mandatoryWithUnknownRules.get(false).size());
            dto.missingRules(select(rules, rule -> !coveredRules.contains(rule), new TreeSet<>()));
            jobDto.addMandatoryStagesItem(dto);
            Pair<Double, Integer> alignmentResult = vaAlignment.get(stage.getName());
            if (alignmentResult != null) {
                dto.setStagePercentage(alignmentResult.getLeft());
                dto.setStageDiff(alignmentResult.getRight());
            }
        } else {
            dto.setUnknownRules(dto.getRules().size());
            jobDto.addUnknownStagesItem(dto);
        }
    }

    private void checkStatusBasedonRulesExecutionOrder(String stageName, StageEntity stage, Map<String, ConditionalExecStatus> conditionalStages, List<MandatoryStageDto> mandatoryStages) {
    	// I need this mandatoryStages to know the order of execution of tasks
    	mandatoryStages.stream().filter(s -> s.getName().equals(stage.getName())).findFirst()
    	.ifPresent(s->
    		s.getRules().stream()
    		.filter(rule -> stage.getRules() != null &&  stage.getRules().contains(rule) && conditionalRulesForAPINBCCheck.containsKey(rule))
    		.forEach(rule -> conditionalStages.put(stageName, conditionalRulesForAPINBCCheck.get(rule)))
    	);
	}

	private long getMaxUnknownStages(final List<JobDto> jobs) {
        return jobs.stream()
                .map(JobDto::getUnknownStages)
                .filter(Objects::nonNull)
                .map(Set::size)
                .max(Integer::compareTo)
                .orElse(0);
    }

    private String getUnknownJobsNamesWithAppTypes(final JobEntity job) {
        return job.getName() + " " + job.getAppType();
    }

    private static String emptyStringIfNull(String str) {
        return str != null ? str : "";
    }

	private static Pair<Double, Boolean> getBuildAlignmentResult(String alignString) {
		 String[] alignments = emptyStringIfNull(alignString).split(";");
		 Double alignPerc = 0.0;
		 Boolean isAligned = null;
		 if(alignments.length == 2) {
				 alignPerc = Double.valueOf(alignments[0]);
				 String val =  alignments[1];
				 isAligned = val.equals("null") ? null : Boolean.parseBoolean(val);
		 }
		 return Pair.of(alignPerc, isAligned);
    }

	public static Map<String, Pair<Double, Integer>> getVAalignmentResult(String alignString) {
	    Map<String, Pair<Double, Integer>> results = new HashMap<>();
	    String[] lines = alignString.split("\n");
	    for (String line : lines) {
	        String[] parts = line.split(":");
	        if (parts.length == 2) {
	            String task = parts[0];
	            String[] alignments = parts[1].split(";");
	            if (alignments.length == 2) {
	                Double alignPerc = Double.valueOf(alignments[0]);
	                Integer isAligned = Integer.valueOf(alignments[1]);
	                results.put(task, Pair.of(alignPerc, isAligned));
	            }
	        }
	    }
	    return results;
	}
}

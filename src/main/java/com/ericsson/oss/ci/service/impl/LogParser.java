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

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.collections4.ListUtils.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ericsson.oss.ci.domain.dto.configuration.Stage;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.entity.StageEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.ericsson.oss.ci.service.ParseResult;

@Component
class LogParser {
    private final ParseHelper parseHelper;
    private final Map<String, Map<JobTypeEnum, List<Stage>>> stagesByAppTypeAndJobType;

    public LogParser(final Stages stages, final ParseHelper parseHelper) {
		this.parseHelper = parseHelper;
        stagesByAppTypeAndJobType = emptyIfNull(stages.getStandards()).stream()
                .collect(Collectors.toUnmodifiableMap(
                        Stages.Standard::getAppType,
                        standard -> Map.of(
                                JobTypeEnum.PUBLISH, unmodifiableList(standard.getPublish()),
                                JobTypeEnum.PRE_CODE_REVIEW, unmodifiableList(standard.getPreCodeReview())
                )));
    }

    ParseResult getParseResult(final String jobLog, final JobTypeEnum jobType, final String appType) {
        final Map<String, Set<String>> standardStages = getStagesWithRules(appType,jobType);
        final StageAccumulator stageAccumulator = new StageAccumulator(standardStages);
        StringUtils.defaultString(jobLog).lines()
                .forEach(line -> processLine(line, stageAccumulator));
        return stageAccumulator;
    }

    private Map<String, Set<String>> getStagesWithRules(final String appType, final JobTypeEnum jobType) {
        return Optional.of(appType)
                .map(stagesByAppTypeAndJobType::get)
                .map(map -> map.get(jobType))
                .orElseThrow(() -> new RuntimeException("Not found mandatory stages for " + appType + " " + jobType)).stream()
                .collect(Collectors.toUnmodifiableMap(Stage::getName, Stage::getRules));
    }

    private void processLine(final String line, final StageAccumulator stageAccumulator) {

        parseHelper.matchStageName(line).ifPresent(stageAccumulator::updateAllStages);
        parseHelper.matchSkippedStage(line).ifPresent(stageAccumulator::updateSkippedStages);

        Optional.of(line)
        .flatMap(parseHelper::matchDockerRunRule)
        .ifPresent(stageAccumulator::addRulesAndTasksToStages);

         Optional.of(line)
         .flatMap(parseHelper::matchRuleAndTask)
         .ifPresent(stageAccumulator::addRulesAndTasksToStages);

         Optional.of(line)
         .flatMap(parseHelper::matchSonarAndK8sRegex)
         .ifPresent(stageAccumulator::addRulesAndTasksToStages);

        Optional.of(line)
                .filter(value -> StringUtils.isBlank(stageAccumulator.cbosVersion))
                .flatMap(parseHelper::matchCbosVersion)
                .ifPresent(stageAccumulator::setCbosVersion);

        Optional.of(line)
        .filter(value -> StringUtils.isBlank(stageAccumulator.sonarUrl))
        .flatMap(parseHelper::matchSonarUrl)
        .ifPresent(stageAccumulator::setSonarUrl);
    }

    private static class StageAccumulator implements ParseResult {
        private final Map<String,Set<String>> standardStages;
        private Set<String> allStagesNames = new HashSet<>();
        private Set<String> skippedStages = new HashSet<>();
        private Map<String, StageEntity> newStages = new HashMap<>();
        private String cbosVersion;
        private String sonarUrl;

         public StageAccumulator(final Map<String,Set<String>> standardStages) {
             this.standardStages = new HashMap<>(standardStages);
        }

        @Override
        public List<StageEntity> getStages() {
        	newStages.entrySet().removeIf(entry -> !allStagesNames.contains(entry.getKey()));
            skippedStages.stream()
			.filter(standardStages::containsKey)
			.forEach(skippedStage -> newStages.put(skippedStage, new StageEntity(skippedStage, new HashSet<>())));
            
            Set<String> skippedStagesSet = new HashSet<>(skippedStages);
            skippedStagesSet.retainAll(allStagesNames);
            
            newStages.put("SKIPPED_STAGES", new StageEntity("SKIPPED_STAGES", skippedStagesSet));
            
            final Set<String> stagesToProcess = new HashSet<>(allStagesNames);
            stagesToProcess.stream()
                 .filter(standardStages::containsKey)
                 .forEach(stage -> {
                      if(!newStages.containsKey(stage)) {
                          newStages.put(stage, new StageEntity(stage, new HashSet<>()));
                       }
                  });
            allStagesNames.removeAll(skippedStages);
            allStagesNames.removeIf(standardStages::containsKey);
            
            newStages.put("CUSTOM_STAGES", new StageEntity("CUSTOM_STAGES", allStagesNames));
            return new ArrayList<>(newStages.values());
        }

        @Override
        public String getCbosVersion() {
            return cbosVersion;
        }

        public void updateAllStages(final String stageName) {
               allStagesNames.add(stageName);
        }

        public void updateSkippedStages(final String skippedStage) {
            	skippedStages.add(skippedStage);
        }

        public void addRulesAndTasksToStages(final String ruleOrTask) {
           standardStages.entrySet().stream().filter(entry -> entry.getValue().contains(ruleOrTask))
            .map(Map.Entry::getKey)
            .forEach(key -> {
                 if(newStages.get(key) == null){
                     newStages.put(key, new StageEntity(key, new HashSet<>()));
                 }
                 newStages.get(key).getRules().add(ruleOrTask);

             });
        }

        private void setCbosVersion(final String cbosVersion) {
            this.cbosVersion = cbosVersion;
        }

        public String getSonarUrl() {
            return sonarUrl;
        }

        public void setSonarUrl(String sonarUrl) {
            this.sonarUrl = sonarUrl;
        }
    }
}

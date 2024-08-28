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

import com.ericsson.oss.ci.domain.dto.configuration.Stage;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.entity.JobEntity;
import com.ericsson.oss.ci.domain.entity.StageEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class TestHelper {

    static Stage getStage(final Set<String> rules) {
        final Stage stage = new Stage();
        stage.setName(getRandomString());
        stage.setRules(rules);
        return stage;
    }

    static Stage getStage(final String stageName, final Set<String> rules) {
        final Stage stage = new Stage();
        stage.setName(stageName);
        stage.setRules(rules);
        return stage;
    }

    static Stages getStages(final List<Stages.Standard> standards) {

        final Stages stages = new Stages();
        stages.setStandards(standards);
        return stages;
    }

    static Stages.Standard getStandard(final String appType,
                                       final List<Stage> publish,
                                       final List<Stage> prc) {
        final Stages.Standard standard = new Stages.Standard();
        standard.setAppType(appType);
        standard.setPublish(publish);
        standard.setPreCodeReview(prc);
        return standard;
    }

    static JobEntity getJobEntity(final String jobName,
                                  final String appType) {
        final JobEntity jobEntity = new JobEntity();
        jobEntity.setName(jobName);
        jobEntity.setAppType(appType);
        jobEntity.setUrl("sampleURL");
        jobEntity.setType(JobTypeEnum.PUBLISH);
        return jobEntity;
    }

    static StageEntity getRandomStageEntity() {
        return getStageEntity(getRandomString(),
                IntStream.range(1, getRandomInt())
                        .mapToObj(i -> getRandomString())
                        .collect(Collectors.toSet()));
    }

    static StageEntity getStageEntity(final String name,
                                      final Set<String> rules) {
        final StageEntity stage = new StageEntity();
        stage.setName(name);
        stage.setRules(rules);
        return stage;
    }

    static List<String> getStrings(final int length) {
        return IntStream.range(1, length)
                .mapToObj(i -> getRandomString())
                .collect(Collectors.toList());
    }

    static String getRandomString(int lines) {
        return IntStream.range(1, lines)
                .mapToObj(i -> getRandomString())
                .collect(Collectors.joining(StringUtils.LF));
    }

    static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    static int getRandomInt() {
        return ThreadLocalRandom.current().nextInt(5, 20);
    }

    static long getRandomLong() {
        return ThreadLocalRandom.current().nextLong(5, 20);
    }
}

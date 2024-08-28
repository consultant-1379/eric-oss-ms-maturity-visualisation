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

import com.ericsson.oss.ci.CoreApplication;
import com.ericsson.oss.ci.jenkins.JenkinsProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CoreApplication.class)
@Slf4j
class RegularExpressionsTest {
	private static final String STAGE_NAMES_FILE_NAME = "/regexp/stage-names.csv";
    private static final String CBOS_VERSION_FILE_NAME = "/regexp/cbos version.csv";
    private static final String SONAR_URL_FILE_NAME = "/regexp/sonar.csv";
    private static final String SONAR_K8S_FILE_NAME = "/regexp/sonar-k8s.csv";
    private static final String DOCKER_RUN_RULE_FILE_NAME = "/regexp/docker-run-rules.csv";
    private static final String RULES_AND_TASKS_FILE_NAME = "/regexp/rules-and-tasks.csv";

    @MockBean
    private JenkinsProvider jenkinsProvider;
    @Autowired
    private ParseHelper parseHelper;

    @ParameterizedTest
    @CsvFileSource(resources = STAGE_NAMES_FILE_NAME, numLinesToSkip = 1, delimiterString = ";")
    void checkStageNameExpression(final String stageName, final String line) {
        parseHelper.matchStageName(line)
                .ifPresentOrElse(stage -> assertEquals(stageName,
                                stage,
                                "Expected stage name '" + stageName + "', but matched as '" + stage + "'; line: " + line),
                                () -> assertEquals(stageName, line));
    }

    @Test
    void checkSkippedStageNameExpression() {

        String testStage = "Generate Docs";
        String testLine = "Stage \"Generate Docs\" skipped due to when conditional";

        parseHelper.matchSkippedStage(testLine)
                .ifPresentOrElse(stage -> assertEquals(testStage,
                                stage,
                                "Expected stage name '" + testStage + "', but matched as '" + stage + "'; line: " + testLine),
                                () -> assertEquals(testStage, testLine));
    }

    @ParameterizedTest
    @CsvFileSource(resources = CBOS_VERSION_FILE_NAME, numLinesToSkip = 1)
    void checkCbosVersionExpression(final String cbosVersion, final String line) {
        parseHelper.matchCbosVersion(line)
                .ifPresentOrElse(stage -> assertEquals(cbosVersion,
                                stage,
                                "Expected stage name '" + cbosVersion + "', but matched as '" + stage + "'; line: " + line),
                        () -> assertEquals(cbosVersion, line));
    }

    @ParameterizedTest
    @CsvFileSource(resources = SONAR_URL_FILE_NAME, numLinesToSkip = 1, delimiterString = ";")
    void checkSonarUrlExpression(final String sonarUrl, final String line) {
        parseHelper.matchSonarUrl(line)
                .ifPresentOrElse(stage -> assertEquals(sonarUrl,
                                stage,
                                "Expected stage name '" + sonarUrl + "', but matched as '" + stage + "'; line: " + line),
                                () -> assertEquals(sonarUrl, line));
    }

    @ParameterizedTest
    @CsvFileSource(resources = SONAR_K8S_FILE_NAME, numLinesToSkip = 1, delimiterString = ";")
    void checksonarAndK8sExpression(final String sonarAndK8s, final String line) {
        parseHelper.matchSonarAndK8sRegex(line)
                .ifPresentOrElse(stage -> assertEquals(sonarAndK8s,
                                stage,
                                "Expected stage name '" + sonarAndK8s + "', but matched as '" + stage + "'; line: " + line),
                                () -> assertEquals(sonarAndK8s, line));
    }

    @ParameterizedTest
    @CsvFileSource(resources = RULES_AND_TASKS_FILE_NAME, numLinesToSkip = 1, delimiterString = ";")
    void ruleAndTaskExpression(final String rule, final String line) {
        parseHelper.matchRuleAndTask(line)
                .ifPresentOrElse(stage -> assertEquals(rule,
                                stage,
                                "Expected rule/task name '" + rule + "', but matched as '" + stage + "'; line: " + line),
                                () -> assertEquals(rule, line));
    }

    @ParameterizedTest
    @CsvFileSource(resources = DOCKER_RUN_RULE_FILE_NAME, numLinesToSkip = 1, delimiterString = ";")
    void dockerRunRuleExpression(final String rule, final String line) {
        parseHelper.matchDockerRunRule(line)
                .ifPresentOrElse(stage -> assertEquals(rule,
                                stage,
                                "Expected stage name '" + rule + "', but matched as '" + stage + "'; line: " + line),
                                () -> assertEquals(rule, line));
    }
}
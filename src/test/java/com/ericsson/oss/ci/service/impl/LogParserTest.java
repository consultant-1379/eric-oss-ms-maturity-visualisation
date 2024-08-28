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

import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomString;
import static com.ericsson.oss.ci.service.impl.TestHelper.getStage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.ci.domain.dto.configuration.Stage;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.entity.StageEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.ericsson.oss.ci.service.ParseResult;

class LogParserTest {
    private LogParser logParser;
    private ParseHelper parseHelper;
    private Stages stages;
    private String rule;
    private Stage stage;
    private String appType;

    @BeforeEach
    void setUp() {
        parseHelper = mock(ParseHelper.class);
        rule = getRandomString();
        appType = getRandomString();
        stage = getStage(Set.of(rule));
        stage.setName(getRandomString());
        stages = TestHelper.getStages(List.of(
                TestHelper.getStandard(appType, List.of(), List.of(stage))
        ));
        logParser = new LogParser(stages, parseHelper);
    }

    @Test
    void getStagesFromLog_empty() {
              assertThrows(RuntimeException.class, ()->logParser.getParseResult(null,null,null));
              assertEquals(2, logParser.getParseResult("",JobTypeEnum.PUBLISH,appType).getStages().size());
    }

    @Test
    void getStagesFromLog_empty_without_stages() {
             final String log = TestHelper.getRandomString(TestHelper.getRandomInt());
             when(parseHelper.matchDockerRunRule(eq(log))).thenReturn(Optional.empty());
             when(parseHelper.matchRuleAndTask(eq(log))).thenReturn(Optional.empty());
             assertEquals(2, logParser.getParseResult("",JobTypeEnum.PUBLISH,appType).getStages().size());
    }

    @Test
    void getCbosVersion() {
        final String cbosVersion = TestHelper.getRandomString();
        final String log = getString(TestHelper.getRandomString(TestHelper.getRandomInt()), cbosVersion, TestHelper.getRandomString(TestHelper.getRandomInt()));
        when(parseHelper.matchCbosVersion(eq(cbosVersion))).thenReturn(Optional.of(cbosVersion));
        final ParseResult result = logParser.getParseResult(log, JobTypeEnum.PUBLISH, appType);
        assertEquals(cbosVersion, result.getCbosVersion());
    }
    @Test
    void getSonarUrl() {
        final String sonarUrl = TestHelper.getRandomString();
        final String log = getString(TestHelper.getRandomString(TestHelper.getRandomInt()), sonarUrl, TestHelper.getRandomString(TestHelper.getRandomInt()));
        when(parseHelper.matchSonarUrl(eq(sonarUrl))).thenReturn(Optional.of(sonarUrl));
        final ParseResult result = logParser.getParseResult(log, JobTypeEnum.PUBLISH, appType);
        assertEquals(sonarUrl, result.getSonarUrl());
    }

    @Test
    void getStagesFromLog_stage_with_rules() {
        final StageEntity stage1 = new StageEntity(stage.getName(), new HashSet<>(stage.getRules()));
        final String log = TestHelper.getRandomString();
        when(parseHelper.matchDockerRunRule(eq(log))).thenReturn(Optional.of(rule));
        when(parseHelper.matchRuleAndTask(eq(log))).thenReturn(Optional.of(rule));
        when(parseHelper.matchStageName(eq(log))).thenReturn(Optional.of(stage.getName()));
        when(parseHelper.matchSkippedStage(eq(log))).thenReturn(Optional.of(rule));
        final List<StageEntity> result = logParser.getParseResult(log, JobTypeEnum.PRE_CODE_REVIEW, appType).getStages();
        assertFalse(result.isEmpty());
        checkResult(result, stage1);
    }

    private void checkResult(final List<StageEntity> results, final StageEntity stage) {
        results.stream()
                .filter(item -> stage.getName().equals(item.getName()))
                .findFirst()
                .ifPresentOrElse(result  -> {
                    assertEquals(stage.getName(), result.getName());
                    assertTrue(CollectionUtils.containsAll(stage.getRules(), result.getRules()));
                    assertTrue(CollectionUtils.containsAll(result.getRules(), stage.getRules()));
                }, () -> fail("Stage " + stage.getName() + " not found") );
    }

    private String getString(String... strings) {
        return String.join(StringUtils.LF, strings);
    }

}
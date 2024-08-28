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



import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import com.ericsson.oss.ci.domain.dto.configuration.RegularExpressions;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParseHelper {
	private final Pattern stageName;
    private final Pattern cbosVersion;
    private final Pattern sonarUrl;
    private final Pattern ruleAndTaskRegex;
    private final Pattern dockerRunRuleRegex;
    private final Pattern sonarAndK8sRegex;
    private final Pattern skippedStageRegex;

    public ParseHelper(@NonNull final RegularExpressions regularExpressions) {
        stageName = Pattern.compile(regularExpressions.getStageNameRegexp());
        skippedStageRegex = Pattern.compile(regularExpressions.getSkippedStageNameRegexp());
        cbosVersion = Pattern.compile(regularExpressions.getCbosVersionRegexp());
        sonarUrl = Pattern.compile(regularExpressions.getSonarRegexp());
        ruleAndTaskRegex = Pattern.compile(regularExpressions.getRuleAndTaskRegex(), Pattern.CASE_INSENSITIVE);
        dockerRunRuleRegex = Pattern.compile(regularExpressions.getDockerRunRuleRegex());
        sonarAndK8sRegex = Pattern.compile(regularExpressions.getSonarAndK8sRegex());
        logRegularExpressions(regularExpressions);
    }

    Optional<String> matchStageName(final String line) {
        return Optional.ofNullable(line)
                .map(stageName::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1));
    }

    Optional<String> matchSkippedStage(final String line) {
        return Optional.ofNullable(line)
                .map(skippedStageRegex::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1));
    }

    Optional<String> matchDockerRunRule(final String line) {
        return Optional.ofNullable(line)
                .map(dockerRunRuleRegex::matcher)
                .filter(Matcher::find)
                .map(matcher -> {
                     if(matcher.group(4) != null) {
                        if(matcher.group(4).equals(" lint:vale")) {
                            return "Bob Rule: lint:markdownlint lint:vale";
                        }
                        else {
                            return "Bob Rule:"+ matcher.group(4);
                        }
                     }else{
                         return "";
                  }
                });
    }

    Optional<String> matchRuleAndTask(final String line) {
        return Optional.ofNullable(line)
                .map(ruleAndTaskRegex::matcher)
                .filter(Matcher::find)
                .map(matcher -> {
                    if(matcher.group(1) != null) {
                         return "Bob Task: "+ matcher.group(1);
                    }else{
                         String taskName = ":" + matcher.group(3) ;
                        return "Bob Rule: " + matcher.group(2) + ("none".equalsIgnoreCase(matcher.group(3)) ? "" : taskName);
                    }
                });
    }

    Optional<String> matchSonarAndK8sRegex(final String line) {
        return Optional.ofNullable(line)
                .map(sonarAndK8sRegex::matcher)
                .filter(Matcher::find)
                .map(matcher -> {
                    String group1 = matcher.group(1);
                    if(group1 != null && !group1.equals("Lock acquired")) {
                             return "Cmd: " + group1;
                    }
                    return group1;
                });
    }

    Optional<String> matchCbosVersion(final String line) {
        return Optional.ofNullable(line)
                .map(cbosVersion::matcher)
                .filter(Matcher::matches)
                .map(matcher -> {
                	String group1 = matcher.group(1);
                    String group2 = matcher.group(2);
                    String group3 = matcher.group(3);
                    String group4 = matcher.group(4);
                    String group5 = matcher.group(5);
                    String group6 = matcher.group(6);
                    if (group1 != null) {
                        return group1;
                    } else if (group2 != null) {
                        return group2;
                    }
                    else if (group3 != null) {
                        return group3;
                    }
                    else if (group4 != null) {
                        return group4;
                    }
                    else if (group5 != null) {
                        return group5;
                    }
                    else {
                        return group6;
                    }
                	});
    }

    Optional<String> matchSonarUrl(final String line) {
        return Optional.ofNullable(line)
                .map(sonarUrl::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1));
    }

    private void logRegularExpressions(final RegularExpressions regularExpressions) {
        log.info("CBOS version regular expression: {}", regularExpressions.getCbosVersionRegexp());
    }
}

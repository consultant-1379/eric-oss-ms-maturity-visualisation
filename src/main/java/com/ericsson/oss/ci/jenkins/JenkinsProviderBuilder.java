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
package com.ericsson.oss.ci.jenkins;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.exception.IncorrectConfigurationException;
import com.offbytwo.jenkins.client.JenkinsHttpClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class JenkinsProviderBuilder {

    private final Stages stages;
    private final List<Job> jobs;
    private final Set<String> products;

    public JenkinsProviderBuilder(final Stages stages,
                                  final JobsParser jobsParser) throws IOException {
        this.stages = stages;
        this.jobs = jobsParser.loadJobs();
        this.products = jobsParser.extractAllProducts();
        checkConfiguration();
    }

    @Bean
    public JenkinsProvider createJenkinsProvider(@Value("${jenkins.username}") final Optional<String> userName,
                                                 @Value("${jenkins.token}") final Optional<String> token) throws IOException {
        if (isEmptyCredentials(userName, token)) {
            log.warn("Jenkins Username or token is empty");
            return new EmptyJenkinsProvider();
        }
        final Map<String, List<Job>> jobsByJenkinsUrl = jobs.stream()
                .collect(groupingBy(Job::getJenkinsUrl));
        final Map<String, JenkinsServerClient> jenkinsServers = new HashMap<>();
        final List<Job> existingJobs = new ArrayList<>();
        boolean success = true;
        for (Map.Entry<String, List<Job>> entry : jobsByJenkinsUrl.entrySet()) {
            final JenkinsServerClient jenkinsServer = new JenkinsServerClientImpl(entry.getKey(), userName.get(), token.get());
            final String jenkinsVersion = jenkinsServer.getVersion().getLiteralVersion();
            if (JenkinsHttpClient.EMPTY_VERSION.equals(jenkinsVersion) || isBlank(jenkinsVersion)) {
                log.error("Cannot connect to the jenkins '{}'  using '{}'", entry.getKey(), userName.get());
                success = false;
            } else {
                log.info("Connected to the jenkins {} version {}", entry.getKey(), jenkinsVersion);
                existingJobs.addAll(getExistingJobs(entry.getKey(), entry.getValue(), jenkinsServer));
                jenkinsServers.put(entry.getKey(), jenkinsServer);
            }
        }
        if (!success) {
            throw new IncorrectConfigurationException("Cannot connect to the jenkins ");
        }
        return new JenkinsProviderImpl(jenkinsServers, existingJobs, products);
    }

    private List<Job> getExistingJobs(final String jenkinsUrl,
                                      final List<Job> jobs,
                                      final JenkinsServerClient jenkinsServer) throws IOException {
        final Set<String> serverJobsNames = jenkinsServer.getJobs().values().stream()
                .map(com.offbytwo.jenkins.model.Job::getName)
                .collect(Collectors.toSet());
        final Map<Boolean, List<Job>> jobsFoundInJenkinsMap = jobs.stream()
                .collect(partitioningBy(job -> serverJobsNames.contains(job.getJobName())));
        jobsFoundInJenkinsMap.get(Boolean.FALSE)
                .forEach(job -> log.warn("Job '{}' not found on {}  and skip", job.getJobName(), jenkinsUrl));
        return jobsFoundInJenkinsMap.get(Boolean.TRUE);
    }

    private boolean isEmptyCredentials(final Optional<String> userName, final Optional<String> token) {
        return userName.isEmpty()
                || StringUtils.isBlank(userName.get())
                || token.isEmpty()
                || StringUtils.isBlank(token.get());
    }

    private void checkConfiguration() {
        checkStandardsPublishAnPreCodeReviewStages();
        checkJobsAppTypes();
        checkAppTypesDuplicates();
    }

    private void checkAppTypesDuplicates() {
        final Map<String, Long> appTypesCount = emptyIfNull(stages.getStandards()).stream()
                .map(Stages.Standard::getAppType)
                .collect(groupingBy(Function.identity(), Collectors.counting()));
        appTypesCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .findFirst()
                .ifPresent(entry -> {throw new IncorrectConfigurationException("App type '" + entry.getKey() + "' defined more than once in the standards configuration");});
    }

    private void checkJobsAppTypes() {
        final Set<String> standardsAppTypes = emptyIfNull(stages.getStandards()).stream()
                .map(Stages.Standard::getAppType)
                .collect(Collectors.toSet());
        jobs.stream()
                .filter(job -> !standardsAppTypes.contains(job.getAppType()))
                .findFirst()
                .ifPresent(job -> {throw new IncorrectConfigurationException("App type '" + job.getAppType() + "' from job '" + job.getJobName() + "' not found in the configuration");});
    }

    private void checkStandardsPublishAnPreCodeReviewStages() {
        emptyIfNull(stages.getStandards()).stream()
                .filter(standard -> isEmpty(standard.getPreCodeReview()) || isEmpty(standard.getPublish()))
                .findFirst()
                .ifPresent(standard -> {throw new IncorrectConfigurationException("Publish or PreCodeReview stages not found in standard with app type '" + standard.getAppType() + "'");});
    }

    @Slf4j
    private static class EmptyJenkinsProvider implements JenkinsProvider {

        @Override
        public Optional<JenkinsServerClient> getJenkinsServer(final String jenkinsUrl) {
            logEmptyJenkinsCredentialsMessage();
            return Optional.empty();
        }

        @Override
        public List<Job> getJobs() {
            logEmptyJenkinsCredentialsMessage();
            return Collections.emptyList();
        }

        private void logEmptyJenkinsCredentialsMessage() {
            log.warn("Username or token is missing. Application could not read information from jenkins");
        }

        @Override
        public Set<String> getProducts() {
            log.warn("Error while reading products from jobs.csv");
            return Collections.emptySet();
        }
    }
}

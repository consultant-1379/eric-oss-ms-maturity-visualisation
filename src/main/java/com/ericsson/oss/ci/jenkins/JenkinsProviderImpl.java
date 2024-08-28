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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.ci.domain.dto.configuration.Job;

import lombok.Getter;
import lombok.NonNull;

public class JenkinsProviderImpl implements JenkinsProvider {

    @Getter
    private final List<Job> jobs;
    @Getter final Set<String> products;
    private final Map<String, JenkinsServerClient> jenkinsServersByUrl;

    public JenkinsProviderImpl(@NonNull final Map<String, JenkinsServerClient> jenkinsServers,
                               final List<Job> jobs, final Set<String> products) {
        this.jenkinsServersByUrl = Collections.unmodifiableMap(jenkinsServers);
        this.jobs = Collections.unmodifiableList(jobs);
        this.products = Collections.unmodifiableSet(products);
    }

    @Override
    public Optional<JenkinsServerClient> getJenkinsServer(final String jenkinsUrl) {
        return Optional.ofNullable(jenkinsUrl)
                .map(jenkinsServersByUrl::get);
    }
}

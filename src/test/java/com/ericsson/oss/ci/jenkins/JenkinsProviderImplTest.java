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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ericsson.oss.ci.domain.dto.configuration.Job;

class JenkinsProviderImplTest {


    @Test
    void getJenkinsServerEmptyTest() {
        final String url = getRandomString();
        final JenkinsServerClient server = getJenkinsServer();
        final Map<String, JenkinsServerClient> map = Map.of(url, server);
        final JenkinsProviderImpl jenkinsProviderImpl = new JenkinsProviderImpl(map, Collections.emptyList(), Collections.emptySet());
        final Optional<JenkinsServerClient> result =  jenkinsProviderImpl.getJenkinsServer(getRandomString());
        assertTrue(result.isEmpty());
    }

    @Test
    void getJenkinsServerTest() {
        final String url = getRandomString();
        final JenkinsServerClient server = getJenkinsServer();
        final Map<String, JenkinsServerClient> map = Map.of(url, server);
        final JenkinsProviderImpl jenkinsProviderImpl = new JenkinsProviderImpl(map, Collections.emptyList(), Collections.emptySet());
        final Optional<JenkinsServerClient> result =  jenkinsProviderImpl.getJenkinsServer(url);
        assertTrue(result.isPresent());
        assertEquals(server, result.get());
    }

    @Test
    void getJobsTest() {
        final List<Job> jobs = List.of(getJob(), getJob());
        final Set<String> products = Set.of("ADC");
        final JenkinsProviderImpl jenkinsProviderImpl = new JenkinsProviderImpl(Collections.emptyMap(), jobs, products);
        assertEquals(jobs, jenkinsProviderImpl.getJobs());
    }

    @Test
    void unmodifiableJobsTest() {
        final List<Job> jobs =  new ArrayList<>();
        final Set<String> products = new HashSet<>();
        jobs.add(getJob());
        jobs.add(getJob());
        JenkinsProviderImpl jenkinsProviderImpl = new JenkinsProviderImpl(Collections.emptyMap(), jobs, products);
        assertThrows(UnsupportedOperationException.class, () -> jenkinsProviderImpl.getJobs().add(getJob()));
    }

    private String getRandomString() {
        return UUID.randomUUID().toString();
    }

    private JenkinsServerClient getJenkinsServer() {
        return mock(JenkinsServerClient.class);
    }

    private Job getJob() {
        return new Job();
    }
}
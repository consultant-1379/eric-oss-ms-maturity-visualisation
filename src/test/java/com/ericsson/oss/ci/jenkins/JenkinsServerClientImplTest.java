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

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.helper.JenkinsVersion;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JenkinsServerClientImplTest {


    @Test
    void testGetJobs() throws IOException {
        final JenkinsServer jenkinsServer = mock(JenkinsServer.class);
        final JenkinsServerClientImpl jenkinsServerClient = getJenkinsServerClient(jenkinsServer);
        final Map<String, Job> jobs = Collections.emptyMap();
        when(jenkinsServer.getJobs()).thenReturn(jobs);
        Assertions.assertEquals(jobs, jenkinsServerClient.getJobs());
    }

    @Test
    void testGetJobsError() throws IOException {
        final JenkinsServer jenkinsServer = mock(JenkinsServer.class);
        final JenkinsServerClientImpl jenkinsServerClient = getJenkinsServerClient(jenkinsServer);
        when(jenkinsServer.getJobs()).thenThrow(new IOException());
        Assertions.assertThrows(IOException.class, jenkinsServerClient::getJobs);
    }

    @Test
    void testGetJob() throws IOException {
        final JenkinsServer jenkinsServer = mock(JenkinsServer.class);
        final JenkinsServerClientImpl jenkinsServerClient = getJenkinsServerClient(jenkinsServer);
        final String jobName = getRandomString();
        final JobWithDetails job = new JobWithDetails();
        when(jenkinsServer.getJob(eq(jobName))).thenReturn(job);
        Assertions.assertEquals(job, jenkinsServerClient.getJob(jobName));
    }

    @Test
    void testGetJobError() throws IOException {
        final JenkinsServer jenkinsServer = mock(JenkinsServer.class);
        final JenkinsServerClientImpl jenkinsServerClient = getJenkinsServerClient(jenkinsServer);
        when(jenkinsServer.getJob(any())).thenThrow(new IOException());
        Assertions.assertThrows(IOException.class, () -> jenkinsServerClient.getJob(getRandomString()));
    }

    @Test
    void testVersion() {
        final JenkinsServer jenkinsServer = mock(JenkinsServer.class);
        final JenkinsServerClientImpl jenkinsServerClient = getJenkinsServerClient(jenkinsServer);
        final JenkinsVersion version = new JenkinsVersion();
        when(jenkinsServer.getVersion()).thenReturn(version);
        Assertions.assertEquals(version, jenkinsServerClient.getVersion());
    }

    private JenkinsServerClientImpl getJenkinsServerClient(final JenkinsServer jenkinsServer) {
        final JenkinsServerClientImpl jenkinsServerClient = new JenkinsServerClientImpl(
                getRandomString(),
                getRandomString(),
                getRandomString()
        );
        ReflectionTestUtils.setField(jenkinsServerClient, "jenkinsServer", jenkinsServer);
        return jenkinsServerClient;
    }

    private String getRandomString() {
        return UUID.randomUUID().toString();
    }
}
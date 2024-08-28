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

import java.io.IOException;
import java.net.URI;
import java.util.Map;

class JenkinsServerClientImpl implements JenkinsServerClient {
    private final JenkinsServer jenkinsServer;

    JenkinsServerClientImpl(final String url,
                            final String username,
                            final String token) {
        this.jenkinsServer = new JenkinsServer(URI.create(url), username, token);
    }

    @Override
    public Map<String, Job> getJobs() throws IOException {
        return jenkinsServer.getJobs();
    }

    @Override
    public JobWithDetails getJob(final String jobName) throws IOException {
        return jenkinsServer.getJob(jobName);
    }

    @Override
    public JenkinsVersion getVersion() {
        return jenkinsServer.getVersion();
    }
}

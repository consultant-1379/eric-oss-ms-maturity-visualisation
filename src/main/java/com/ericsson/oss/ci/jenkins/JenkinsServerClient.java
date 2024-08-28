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

import com.offbytwo.jenkins.helper.JenkinsVersion;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;

import java.io.IOException;
import java.util.Map;

public interface JenkinsServerClient {
    JobWithDetails getJob(String jobName) throws IOException;

    Map<String, Job> getJobs() throws IOException;

    JenkinsVersion getVersion();
}

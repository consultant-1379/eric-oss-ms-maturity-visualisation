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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ericsson.oss.ci.domain.dto.configuration.Job;

public interface JenkinsProvider {
    Optional<JenkinsServerClient> getJenkinsServer(String jenkinsUrl);

    List<Job> getJobs();

    Set<String> getProducts();
}

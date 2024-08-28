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


import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.client.HttpResponseException;
import org.springframework.stereotype.Service;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.repository.BuildRepository;
import com.ericsson.oss.ci.jenkins.JenkinsProvider;
import com.ericsson.oss.ci.service.JenkinsService;
import com.ericsson.oss.ci.service.JobSaverService;
import com.ericsson.oss.ci.service.ParseResult;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.JobWithDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JenkinsServiceImpl implements JenkinsService {

    private final JenkinsProvider jenkinsProvider;
    private final LogParser logParser;
    private final BuildRepository buildRepository;
    private final JobSaverService jobSaverService;

    @Override
    public Stream<Job> getJobs() {
        return jenkinsProvider.getJobs().stream();
    }

    @Override
    public void processJob(final Job job) {
        try {
        	log.info("entered into process Job {} ", job.toString());
            final JobWithDetails jenkinsJob = jenkinsProvider.getJenkinsServer(job.getJenkinsUrl())
                    .orElseThrow(() -> new RuntimeException("Jenkins server " + job.getJenkinsUrl() + " not found in configuration"))
                    .getJob(job.getJobName());
            List<Build> builds = jenkinsJob.getBuilds();
            List<Build> limit = builds.stream().sorted(Comparator.comparing(Build :: getNumber,Comparator.naturalOrder()))
               .filter(build -> {
                 BuildResult result=null;
				try {
					result = build.details().getResult();
				} catch (HttpResponseException e) {
					log.error("Error during filter to validate the response for JenkinsHttpClient with error statusCode: {}, on Build for buildNo {} job {}", e.getStatusCode(), build.getNumber(), job.getJobName(), e);
					return Boolean.FALSE;
		        }
				catch (IOException e) {
					log.error("Error during filter Build for buildNo {} job {} : {}", build.getNumber(), job.getJobName(), e.getMessage(), e);
					return Boolean.FALSE;
				}
				return result != null && (BuildResult.SUCCESS.equals(result) || BuildResult.FAILURE.equals(result));
               })
            .limit(20).collect(Collectors.toList());
            limit.forEach(jenkinsBuild -> {
            log.info("Processing Build No {}, jenkinsJobName {} ",  jenkinsBuild.getNumber(),jenkinsJob.getName());
            if (jenkinsBuild.getNumber() == -1) {
                    log.info("Skip job {} Does not has builds to analyze;",  jenkinsJob.getName());
                    return;
             }
            if (buildRepository.existsByJobJenkinsUrlAndJobNameAndBuildNo(
                    job.getJenkinsUrl(),
                    jenkinsJob.getName(),
                    jenkinsBuild.getNumber())) {
                    return;
            }
            ParseResult parseResult=null;
            log.info("calling ParseResult.getParseResult() {} {} url: {}",jenkinsBuild.getNumber(),jenkinsJob.getName(), jenkinsJob.getUrl());
			try {
				parseResult = logParser.getParseResult(jenkinsBuild.details().getConsoleOutputText(), job.getType(), job.getAppType());
			}  catch (HttpResponseException e) {
				log.error("Error calling ParseResult.getParseResult() to validate the response for JenkinsHttpClient with error statusCode: {}, on jenkinsUrl: {}", e.getStatusCode(), jenkinsBuild.getUrl(), e);
				return;
	        }
			catch (IOException e) {
				log.error("Error during executing job {}", job.getJobName(), e);
                return;
			}
			log.info("called ParseResult.getParseResult() {} {}",jenkinsBuild.getNumber(),jenkinsJob.getName());
            jobSaverService.saveBuild(job.getJenkinsUrl(), jenkinsJob, jenkinsBuild, parseResult, job);
            log.info("Process and save job {} build ({})", jenkinsJob.getName(), jenkinsBuild.getNumber());
            });
        } catch (IOException e) {
            log.error("Error during process job {}", job.getJobName(), e);
        }
    }
}

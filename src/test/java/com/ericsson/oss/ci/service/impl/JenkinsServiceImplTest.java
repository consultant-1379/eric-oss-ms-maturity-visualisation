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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.entity.StageEntity;
import com.ericsson.oss.ci.domain.repository.BuildRepository;
import com.ericsson.oss.ci.jenkins.JenkinsProvider;
import com.ericsson.oss.ci.jenkins.JenkinsServerClient;
import com.ericsson.oss.ci.service.JenkinsService;
import com.ericsson.oss.ci.service.JobSaverService;
import com.ericsson.oss.ci.service.ParseResult;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;

class JenkinsServiceImplTest {

    private JenkinsService jenkinsService;
    private JenkinsProvider jenkinsProvider;
    private LogParser logParser;
    private BuildRepository buildRepository;
    private JobSaverService jobSaverService;

    @BeforeEach
    void setUp() {
        logParser = mock(LogParser.class);
        buildRepository = mock(BuildRepository.class);
        jenkinsProvider = mock(JenkinsProvider.class);
        jobSaverService = mock(JobSaverService.class);
        jenkinsService = new JenkinsServiceImpl(
                jenkinsProvider,
                logParser,
                buildRepository,
                jobSaverService
        );
    }

    @Test
    void getJobsNamesEmptyTest() {
        when(jenkinsProvider.getJobs()).thenReturn(List.of());
        assertEquals(0, jenkinsService.getJobs().count());
    }

    @Test
    void processJobGetJobErrorTest() throws IOException {
        final String url = getRandomString();
        final Job job = new Job();
        job.setJenkinsUrl(url);
        final JenkinsServerClient jenkinsServer = mock(JenkinsServerClient.class);
        when(jenkinsProvider.getJenkinsServer(eq(url)))
                .thenReturn(Optional.of(jenkinsServer));
        when(jenkinsServer.getJob(any())).thenThrow(new IOException());

        jenkinsService.processJob(job);

        verify(logParser, times(0)).getParseResult(any(), any(), any());
        verify(buildRepository, times(0)).save(any());
        verify(buildRepository, times(0)).existsByJobJenkinsUrlAndJobNameAndBuildNo(any(), any(), anyInt());
        verify(jobSaverService, times(0)).saveBuild(any(), any(), any(), any(), any());
    }

    @Test
    void processJobGetBuildConsoleErrorTest() throws IOException {
        final String url = getRandomString();
        final Job job = new Job();
        job.setJenkinsUrl(url);
        final JenkinsServerClient jenkinsServer = mock(JenkinsServerClient.class);
        when(jenkinsProvider.getJenkinsServer(eq(url)))
                .thenReturn(Optional.of(jenkinsServer));
        final JobWithDetails jobWithDetails = mock(JobWithDetails.class);
        final Build build = mock(Build.class);
        final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        when(build.getNumber()).thenReturn(1);
        when(buildWithDetails.getConsoleOutputText()).thenThrow(new IOException());
        when(build.details()).thenReturn(buildWithDetails);
        when(jobWithDetails.getBuilds()).thenReturn(List.of(build));
        when(jenkinsServer.getJob(any())).thenReturn(jobWithDetails);
        when(buildRepository.existsByJobJenkinsUrlAndJobNameAndBuildNo(any(), any(), anyInt())).thenReturn(false);

        jenkinsService.processJob(job);

        verify(logParser, times(0)).getParseResult(any(), any(), any());
        verify(buildRepository, times(0)).save(any());
        verify(jobSaverService, times(0)).saveBuild(any(), any(), any(), any(), any());
    }

    @Test
    void processJobWithoutBuildTest() throws IOException {
        final String url = getRandomString();
        final Job job = new Job();
        job.setJenkinsUrl(url);
        final JenkinsServerClient jenkinsServer = mock(JenkinsServerClient.class);
        when(jenkinsProvider.getJenkinsServer(eq(url)))
                .thenReturn(Optional.of(jenkinsServer));
        final JobWithDetails jobWithDetails = mock(JobWithDetails.class);
        final Build build = mock(Build.class);
        final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        when(build.getNumber()).thenReturn(-1);
        when(build.details()).thenReturn(buildWithDetails);
        when(jobWithDetails.getBuilds()).thenReturn(List.of(build));
        when(jenkinsServer.getJob(any())).thenReturn(jobWithDetails);

        jenkinsService.processJob(job);

        verify(logParser, times(0)).getParseResult(any(), any(), any());
        verify(buildRepository, times(0)).existsByJobJenkinsUrlAndJobNameAndBuildNo(any(), any(), anyInt());
        verify(buildRepository, times(0)).save(any());
        verify(jobSaverService, times(0)).saveBuild(any(), any(), any(), any(), any());
    }

    @Test
    void processJobExistJob() throws IOException {
        final String url = getRandomString();
        final Job job = new Job();
        job.setJenkinsUrl(url);
        final JenkinsServerClient jenkinsServer = mock(JenkinsServerClient.class);
        when(jenkinsProvider.getJenkinsServer(eq(url)))
                .thenReturn(Optional.of(jenkinsServer));
        initProcessJob(jenkinsServer, Collections.emptyList());

        jenkinsService.processJob(job);

        verify(jobSaverService, times(1)).saveBuild(eq(url), any(), any(), any(), any());
    }

    private void initProcessJob(final JenkinsServerClient jenkinsServer, final List<StageEntity> stages) throws IOException {
        final JobWithDetails jobWithDetails = mock(JobWithDetails.class);
        List<Build> list=new ArrayList<>();
        final Build build = mock(Build.class);
        list.add(build);

        final Build build2 = mock(Build.class);
        list.add(build2);
        when(build2.getNumber()).thenReturn(1);
        when(build2.details()).thenThrow(new IOException());

        final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        when(build.getNumber()).thenReturn(1);
        when(build.details()).thenReturn(buildWithDetails);
        when(build.details().getResult()).thenReturn(BuildResult.SUCCESS);
        when(jobWithDetails.getLastSuccessfulBuild()).thenReturn(build);
        when(jobWithDetails.getBuilds()).thenReturn(list);
        when(jenkinsServer.getJob(any())).thenReturn(jobWithDetails);
        when(buildRepository.existsByJobJenkinsUrlAndJobNameAndBuildNo(getRandomString(), getRandomString(), 0)).thenReturn(false);
        when(logParser.getParseResult(any(), any(), any())).thenReturn(getParseResult(stages));

        ParseResult parseResult = logParser.getParseResult(getRandomString(),null,getRandomString());
        assertNull(parseResult.getCbosVersion());
        assertNull(parseResult.getSonarUrl());
        assertEquals(Collections.emptyList(),parseResult.getStages());
    }

    private ParseResult getParseResult(final List<StageEntity> stages) {
        return new ParseResult() {
            @Override
            public List<StageEntity> getStages() {
                return stages;
            }

            @Override
            public String getCbosVersion() {
                return null;
            }

			@Override
			public String getSonarUrl() {
				return null;
			}
        };
    }
}
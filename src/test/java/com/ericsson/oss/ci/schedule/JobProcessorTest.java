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
package com.ericsson.oss.ci.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.service.JenkinsService;

class JobProcessorTest {

    private JobProcessor jobProcessor;
    private JenkinsService jenkinsService;

    @BeforeEach
    void setUp() {
        jenkinsService = mock(JenkinsService.class);
        jobProcessor = new JobProcessor(jenkinsService);
    }

    @Test
    void processJobs() {
        final Collection<Job> jobs = getJobs();
        final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);

        when(jenkinsService.getJobs()).thenReturn(jobs.stream());
        doNothing().when(jenkinsService).processJob(jobCaptor.capture());

        jobProcessor.processJobs();

        final List<Job> capturedJobs = jobCaptor.getAllValues();
        assertEquals(capturedJobs.size(), jobs.size());
        assertTrue(CollectionUtils.isEqualCollection(jobs, capturedJobs));
    }

    private Collection<Job> getJobs() {
        return IntStream.range(1, ThreadLocalRandom.current().nextInt(1, 10))
                .mapToObj(i -> getJob())
                .collect(Collectors.toList());
    }

    private Job getJob() {
        return new Job();
    }
}
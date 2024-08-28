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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.dto.configuration.Stage;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.exception.IncorrectConfigurationException;
import com.ericsson.oss.ci.domain.repository.ProductRepository;

class JenkinsProviderBuilderTest {

    @ParameterizedTest
    @CsvSource({
            ",",
            "username,",
            ",token",
            "username,' '",
            "' ',' '",
            "' ',token"
    })
    void createEmptyJenkinsProvider(final String username, final String token) throws IOException {
        final JobsParser jobsParser = mock(JobsParser.class);
        when(jobsParser.loadJobs()).thenReturn(emptyList());
        when(jobsParser.extractAllProducts()).thenReturn(emptySet());
        JenkinsProviderBuilder builder = new JenkinsProviderBuilder(
                new Stages(),
                jobsParser
        );
        final JenkinsProvider jenkinsProviderImpl = builder.createJenkinsProvider(
                Optional.ofNullable(username),
                Optional.ofNullable(token));
        assertTrue(jenkinsProviderImpl.getJobs().isEmpty());
        assertTrue(jenkinsProviderImpl.getProducts().isEmpty());
        assertTrue(jenkinsProviderImpl.getJenkinsServer("").isEmpty());
    }

    @Test
    void checkAppTypesDuplicatesTest() {
        final String appType = getRandomString();
        final Stages stages = getStages(List.of(
                getStandard(appType, List.of(getStage(Set.of())), List.of(getStage(Set.of()))),
                getStandard(appType, List.of(getStage(Set.of())), List.of(getStage(Set.of())))));

        final String expectedMessage = "App type '" + appType + "' defined more than once in the standards configuration";
        checkExpectedException(expectedMessage,
                stages,
                mock(JobsParser.class));
    }

    @Test
    void checkJobsAppTypesTest() throws IOException {
        final String appType = getRandomString();
        final String jobName = getRandomString();
        final Stages stages = getStages(getRandomString(),
                List.of(getStage(Set.of())),
                List.of(getStage(Set.of())));
        final JobsParser jobsParser = mock(JobsParser.class);
        when(jobsParser.loadJobs())
                .thenReturn(List.of(getJob(appType, jobName)));
        final String expectedMessage = "App type '" + appType + "' from job '" + jobName + "' not found in the configuration";
        checkExpectedException(expectedMessage,
                stages,
                jobsParser);
    }

    @Test
    void checkStandardsPublishAnPreCodeReviewStagesTest() {
        final String appType = getRandomString();
        final Stage stage = getStage();
        checkEmptyStandardsPublishAnPreCodeReviewStages(appType, null, null);
        checkEmptyStandardsPublishAnPreCodeReviewStages(appType, null, emptyList());
        checkEmptyStandardsPublishAnPreCodeReviewStages(appType, emptyList(), null);
        checkEmptyStandardsPublishAnPreCodeReviewStages(appType, null, List.of(stage));
        checkEmptyStandardsPublishAnPreCodeReviewStages(appType, emptyList(), List.of(stage));
        checkEmptyStandardsPublishAnPreCodeReviewStages(appType, List.of(stage), null);
        checkEmptyStandardsPublishAnPreCodeReviewStages(appType, List.of(stage), emptyList());
    }

    @Test
    void checkJobsInJenkinsTest() throws IOException {
        final String jobName = getRandomString();
        final Job existJob = getJob(getRandomString(), jobName);
        final List<Job> jobs = List.of(existJob, getJob(getRandomString(), getRandomString()));

        final JenkinsServerClient jenkinsServerClient = mock(JenkinsServerClient.class);
        final com.offbytwo.jenkins.model.Job jenkinsJob = getJenkinsJob(jobName);
        when(jenkinsServerClient.getJobs())
                .thenReturn(Map.of(jobName, jenkinsJob));

        final JenkinsProviderBuilder builder = new JenkinsProviderBuilder(new Stages(), mock(JobsParser.class));

        final List<Job> result = ReflectionTestUtils.invokeMethod(builder,
                "getExistingJobs",
                getRandomString(),
                jobs,
                jenkinsServerClient);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(existJob, result.get(0));
    }

    @Test
    public void testMapToJobWithEmptyFields() {
        String csvString = ";;;PMH";
        Reader inputString = new StringReader(csvString);
        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.DEFAULT.builder().setDelimiter(';').build().parse(inputString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CSVRecord record = records.iterator().next();
        ProductRepository productRepository = mock(ProductRepository.class);
        when(productRepository.findFirstByProduct("PMH")).thenReturn(Optional.empty());

        JobTypeMatcher jobTypeMatcher = mock(JobTypeMatcher.class);
        JobsParser jobsParser = new JobsParser(productRepository, jobTypeMatcher);
        Job result = jobsParser.mapToJob(record);
        assertNull(result);
        verify(productRepository, times(1)).findFirstByProduct("PMH");
    }


    private void checkEmptyStandardsPublishAnPreCodeReviewStages(
            final String appType,
            final List<Stage> publish,
            final List<Stage> preCodeReview) {
        final Stages stages = getStages(appType, publish, preCodeReview);
        final JobsParser jobsParser = mock(JobsParser.class);
        final String expectedMessage = "Publish or PreCodeReview stages not found in standard with app type '" + appType + "'";
        checkExpectedException(expectedMessage,
                stages,
                jobsParser);
    }

    private void checkExpectedException(final String expectedMessage,
                                        final Stages stages,
                                        final JobsParser jobsParser) {
        final IncorrectConfigurationException exception = assertThrows(IncorrectConfigurationException.class,
                () -> new JenkinsProviderBuilder(stages, jobsParser));
        assertEquals(expectedMessage, exception.getMessage());
    }

    private String getRandomString() {
        return UUID.randomUUID().toString();
    }

    private Stages getStages(final String appType,
                             final List<Stage> publish,
                             final List<Stage> prc) {
        return getStages(List.of(getStandard(appType, publish, prc)));
    }

    private Stages getStages(final List<Stages.Standard> standards) {

        final Stages stages = new Stages();
        stages.setStandards(standards);
        return stages;
    }

    private Stages.Standard getStandard(final String appType,
                                        final List<Stage> publish,
                                        final List<Stage> prc) {
        final Stages.Standard standard = new Stages.Standard();
        standard.setAppType(appType);
        standard.setPublish(publish);
        standard.setPreCodeReview(prc);
        return standard;
    }

    private com.offbytwo.jenkins.model.Job getJenkinsJob(final String jobName) {
        final com.offbytwo.jenkins.model.Job job = mock(com.offbytwo.jenkins.model.Job.class);
        when(job.getName()).thenReturn(jobName);
        return job;
    }

    private Job getJob(final String appType,
                       final String jobName) {
        final Job job = new Job();
        job.setAppType(appType);
        job.setJobName(jobName);
        return job;
    }

    private Stage getStage() {
        return getStage(emptySet());
    }

    private Stage getStage(final Set<String> rules) {
        final Stage stage = new Stage();
        stage.setName(getRandomString());
        stage.setRules(rules);
        return stage;
    }
}
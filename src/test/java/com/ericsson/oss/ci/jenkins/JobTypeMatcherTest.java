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

import com.ericsson.oss.ci.domain.dto.configuration.JobTypeConfiguration;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobTypeMatcherTest {

    @Test
    void emptyTypeTest() {
        JobTypeMatcher jobTypeMatcher = getJobTypeMatcher(
                List.of(getRandomString(), getRandomString()),
                List.of(getRandomString(),  getRandomString()),
                getRandomString(),
                getRandomString()
        );
        final Optional<JobTypeEnum> result = jobTypeMatcher.getJobTypeByName(getRandomString());
        assertTrue(result.isEmpty());
    }

    @Test
    void prcTypeTest() {
        final String prc = getRandomString();
        JobTypeMatcher jobTypeMatcher = getJobTypeMatcher(
                List.of(getRandomString(), getRandomString()),
                List.of(prc,  getRandomString()),
                getRandomString(),
                getRandomString()
        );
        final Optional<JobTypeEnum> result = jobTypeMatcher.getJobTypeByName(prc);
        assertTrue(result.isPresent());
        assertEquals(JobTypeEnum.PRE_CODE_REVIEW, result.get());
    }

    @Test
    void publishTypeTest() {
        final String publish = getRandomString();
        JobTypeMatcher jobTypeMatcher = getJobTypeMatcher(
                List.of(publish,  getRandomString()),
                List.of(getRandomString(), getRandomString()),
                getRandomString(),
                getRandomString()
        );
        final Optional<JobTypeEnum> result = jobTypeMatcher.getJobTypeByName(publish);
        assertTrue(result.isPresent());
        assertEquals(JobTypeEnum.PUBLISH, result.get());
    }

    @ParameterizedTest
    @CsvSource({
            "PUBLISH,publish,publish,prc",
            "PRE_CODE_REVIEW,prc,publish,prc",
            ",,publish,prc"
    })
    void getJobTypeLabelTest(final JobTypeEnum type,
                             final String expectedLabel,
                             final String publishLabel,
                             final String preCodeReviewLabel) {
        final JobTypeMatcher jobTypeMatcher = getJobTypeMatcher(
                Collections.emptyList(),
                Collections.emptyList(),
                publishLabel,
                preCodeReviewLabel
        );
        assertEquals(expectedLabel, jobTypeMatcher.getJobTypeLabel(type));
    }

    private JobTypeMatcher getJobTypeMatcher(
            final List<String> publish,
            final List<String> preCodeReview,
            final String publishLabel,
            final String preCodeReviewLabel) {
        return new JobTypeMatcher(getJobTypeConfiguration(
                publish,
                preCodeReview,
                publishLabel,
                preCodeReviewLabel));
    }

    private JobTypeConfiguration getJobTypeConfiguration(
            final List<String> publish,
            final List<String> preCodeReview,
            final String publishLabel,
            final String preCodeReviewLabel) {
        final JobTypeConfiguration config = new JobTypeConfiguration();
        config.setPublish(publish);
        config.setPublishLabel(publishLabel);
        config.setPreCodeReview(preCodeReview);
        config.setPreCodeReviewLabel(preCodeReviewLabel);
        return config;
    }

    private String getRandomString() {
        return UUID.randomUUID().toString();
    }
}
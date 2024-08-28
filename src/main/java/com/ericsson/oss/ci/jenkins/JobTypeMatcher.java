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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.unmodifiableCollection;

@Component
public class JobTypeMatcher {
    private final Collection<String> publish;
    private final Collection<String> preCodeReview;
    private final String publishLabel;
    private final String preCodeReviewLabel;

    @Autowired
    public JobTypeMatcher(final JobTypeConfiguration jobTypeConfiguration) {
        this.publish = unmodifiableCollection(jobTypeConfiguration.getPublish());
        this.preCodeReview = unmodifiableCollection(jobTypeConfiguration.getPreCodeReview());
        this.publishLabel = jobTypeConfiguration.getPublishLabel();
        this.preCodeReviewLabel = jobTypeConfiguration.getPreCodeReviewLabel();
    }

    public Optional<JobTypeEnum> getJobTypeByName(final String jobName) {
        if (isMatch(publish, jobName)) {
            return Optional.of(JobTypeEnum.PUBLISH);
        } else if (isMatch(preCodeReview, jobName)) {
            return Optional.of(JobTypeEnum.PRE_CODE_REVIEW);
        }
        return Optional.empty();
    }

    public String getJobTypeLabel(final JobTypeEnum jobType) {
        if (JobTypeEnum.PUBLISH.equals(jobType)) {
            return publishLabel;
        } else if (JobTypeEnum.PRE_CODE_REVIEW.equals(jobType)) {
            return preCodeReviewLabel;
        }
        return null;
    }

    private boolean isMatch(final Collection<String> values, final String jobName) {
        for (String value : values) {
            if (jobName.contains(value)) {
                return true;
            }
        }
        return false;
    }
}

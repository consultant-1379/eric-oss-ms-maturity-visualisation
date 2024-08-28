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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.entity.ProductEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.ericsson.oss.ci.domain.exception.IncorrectConfigurationException;
import com.ericsson.oss.ci.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
class JobsParser {
    private static final int JENKINS_URL_COLUMN_INDEX = 0;
    private static final int JOB_NAME_COLUMN_INDEX = 1;
    private static final int APP_TYPE_COLUMN_INDEX = 2;
    private static final int PRODUCTS_COLUMN_INDEX = 3;
    private final ProductRepository productRepository;
    private static final String JOBS_FILE_NAME = "config/jobs.csv";
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder
            .create()
            .setSkipHeaderRecord(true)
            .setHeader()
            .setDelimiter(";")
            .build();

    private final JobTypeMatcher jobTypeMatcher;

    List<Job> loadJobs() throws IOException {
        final String fileName = JOBS_FILE_NAME;
        try (final Reader reader = new InputStreamReader(new ClassPathResource(fileName).getInputStream(), StandardCharsets.UTF_8)) {
             return CSV_FORMAT.parse(reader)
                    .getRecords().stream()
                    .map(this::mapToJob)
                    .filter(job->job!=null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            throw e;
        }
    }

    public Set<String> extractAllProducts() throws IOException {
        final String fileName = JOBS_FILE_NAME;
        try (final Reader reader = new InputStreamReader(new ClassPathResource(fileName).getInputStream(), StandardCharsets.UTF_8)) {
             return CSV_FORMAT.parse(reader)
                    .getRecords().stream()
                    .flatMap(record -> getProducts(record.get(PRODUCTS_COLUMN_INDEX)).stream())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            throw e;
        }
    }

    public Job mapToJob(final CSVRecord record) {
    	if(record.get(JENKINS_URL_COLUMN_INDEX).equals("")&& record.get(JOB_NAME_COLUMN_INDEX).equals("")
    			&&record.get(APP_TYPE_COLUMN_INDEX).equals("")){
    		Set<String> products = getProducts(record.get(PRODUCTS_COLUMN_INDEX));
    		products.stream().forEach(p->{
    			Optional<ProductEntity> findFirstByProduct = productRepository.findFirstByProduct(p);
    			if(findFirstByProduct.isEmpty()) {
    			final ProductEntity productEntity = new ProductEntity();
                productEntity.setProduct(p);
                 productRepository.save(productEntity);
    			}
    		});

    			}
    	else {
    		return new Job(
                    getStringValue(record, JENKINS_URL_COLUMN_INDEX, "Jenkins url is required "),
                    getStringValue(record, JOB_NAME_COLUMN_INDEX, "Job name is required "),
                    getStringValue(record, APP_TYPE_COLUMN_INDEX, "App type is required "),
                    getProducts(record.get(PRODUCTS_COLUMN_INDEX)),
                    getJobType(record.get(JOB_NAME_COLUMN_INDEX)));
	}
		return null;

    }

    private String getStringValue(final CSVRecord record,
                                  final int index,
                                  final String errorMessage) {
        return Optional.of(index)
                .map(record::get)
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new IncorrectConfigurationException(errorMessage + " file: " + JOBS_FILE_NAME + " record:" + record.getRecordNumber()));
    }

    private JobTypeEnum getJobType(final String jobName) {
        return jobTypeMatcher.getJobTypeByName(jobName)
                .orElseThrow(() -> new IncorrectConfigurationException("Job " + jobName + " not match to any known job type"));
    }

    private Set<String> getProducts(final String products) {
        return Optional.ofNullable(products)
                .map(value -> value.split(","))
                .stream()
                .flatMap(Arrays::stream)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }
}

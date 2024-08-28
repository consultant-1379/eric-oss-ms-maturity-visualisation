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

import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomInt;
import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomLong;
import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomString;
import static org.apache.commons.collections4.CollectionUtils.emptyCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.entity.BuildEntity;
import com.ericsson.oss.ci.domain.entity.JobEntity;
import com.ericsson.oss.ci.domain.entity.ProductEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.ericsson.oss.ci.domain.repository.BuildRepository;
import com.ericsson.oss.ci.domain.repository.JobRepository;
import com.ericsson.oss.ci.domain.repository.ProductRepository;
import com.ericsson.oss.ci.jenkins.JenkinsProvider;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.MetaData;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ProductResponseDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ProductWiseMetadata;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportListDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportsDto;
import com.ericsson.oss.ci.service.ReportService;

class ReportServiceImplTest {

    private JobRepository jobRepository;
    private ProductRepository productRepository;
    private BuildRepository buildRepository;
    private ReportMapperHelper reportMapperHelper;
    private JenkinsProvider jenkinsProvider;

    @Test
    void getJobsReportEmptyTest() {
        final String ungroupedProductName = getRandomString();
        final String appType = getRandomString();
        final String type = getRandomString();
        final Integer pageNo = getRandomInt();
        final Integer pageSize = getRandomInt();
        final ReportService reportService = getReportService(ungroupedProductName);
        when(jobRepository.findByProductsEmpty())
                .thenReturn(emptyCollection());
        final ReportsDto reportsDto = reportService.getProductReports(ungroupedProductName,appType,type,pageNo,pageSize);
        final ReportListDto reportListDto = reportService.getProductReport(ungroupedProductName,pageNo,pageSize);
        assertTrue(reportsDto.getReports().equals(new ReportDto()));
        assertTrue(reportsDto.getUnknownJobNames().isEmpty());
        assertTrue(reportListDto.getReports().isEmpty());
        assertTrue(reportListDto.getUnknownJobNames().isEmpty());
    }

    @Test
    void getProductsTest() {
        final String ungroupedProductName = getRandomString();
        final ReportService reportService = getReportService(ungroupedProductName);
        final Set<String> lastWeekAddedJobs = new HashSet<>();
        lastWeekAddedJobs.add("Job1");
        lastWeekAddedJobs.add("Job2");

        List<String> productsWithTotalJobs = new ArrayList<>();
        productsWithTotalJobs.add("ADC,4");

        List<List<String>> productsWithTypes = new ArrayList<>();
        productsWithTypes.add(Arrays.asList("ADC", "java", "3"));

        when(jobRepository.getJobs()).thenReturn(lastWeekAddedJobs);
        when(productRepository.getJobsByEachProduct()).thenReturn(productsWithTotalJobs);
        when(jobRepository.getAppTypeAndTypes()).thenReturn(productsWithTypes);

        MetaData expectedMetaData = MetaData.builder()
                .section("java")
                .totalJobs("3")
                .build();

        List<MetaData> metaDataList = new ArrayList<>();
        metaDataList.add(expectedMetaData);

        ProductWiseMetadata expectedProductWiseMetadata = ProductWiseMetadata.builder()
                .product("ADC")
                .metaData(metaDataList)
                .totalJobs("4")
                .build();

        List<ProductWiseMetadata> expectedMetadataList = new ArrayList<>();
        expectedMetadataList.add(expectedProductWiseMetadata);

        ProductResponseDto expectedResponse = ProductResponseDto.builder()
                .jobNotification(lastWeekAddedJobs)
                .productWiseMetadata(expectedMetadataList)
                .build();

        ProductResponseDto result = reportService.getProducts();

        assertEquals(expectedResponse.getJobNotification(), result.getJobNotification());
        assertEquals(expectedResponse.getProductWiseMetadata(), result.getProductWiseMetadata());
    }

    @Test
    void getProductReportsTest() {
        final ReportService reportService = getReportService();
        final ReportsDto reportsDto = new ReportsDto();
        final String product = getRandomString();
        final String appType = getRandomString();
        final String type = getRandomString();
        final Integer pageNo = getRandomInt();
        final Integer pageSize = getRandomInt();
        final JobEntity jobEntity = getJobEntity();
        final BuildEntity buildEntity = new BuildEntity();
        final ProductEntity productEntity = new ProductEntity();
        productEntity.setId(getRandomLong());
        when(productRepository.findFirstByProduct(eq(product)))
                .thenReturn(Optional.of(productEntity));
        Slice<JobEntity> slice = new SliceImpl<>(List.of(jobEntity));
        when(jobRepository.findByProductId(any(),any(),any(),any()))
                .thenReturn(slice);
        when(buildRepository.findFirstByJobAndBuildStatusOrderByBuildNoDesc(eq(jobEntity), eq("SUCCESS")))
                .thenReturn(Optional.of(buildEntity));
        final Pair<JobEntity, BuildEntity> pair = Pair.of(jobEntity, buildEntity);

        final ArgumentCaptor<List> jobsCapture = ArgumentCaptor.forClass(List.class);
        when(reportMapperHelper.map(eq(Boolean.FALSE),jobsCapture.capture()))
                .thenReturn(reportsDto);

        final ReportsDto result = reportService.getProductReports(product,appType,type,pageNo,pageSize);
        assertEquals(reportsDto, result);
        assertTrue(jobsCapture.getValue().contains(pair));
    }
    
    @Test
    void getProductReportTest() {
        final ReportService reportService = getReportService();
        final ReportListDto reportListDto = new ReportListDto();
        final String product = getRandomString();
        final Integer pageNo = getRandomInt();
        final Integer pageSize = getRandomInt();
        final JobEntity jobEntity = getJobEntity();
        final BuildEntity buildEntity = new BuildEntity();
        final ProductEntity productEntity = new ProductEntity();
        productEntity.setId(getRandomLong());
        when(productRepository.findFirstByProduct(eq(product)))
                .thenReturn(Optional.of(productEntity));
        Slice<JobEntity> slice = new SliceImpl<>(List.of(jobEntity));
        when(jobRepository.findByProduct(any(),any()))
                .thenReturn(slice);
        when(buildRepository.findFirstByJobAndBuildStatusOrderByBuildNoDesc(eq(jobEntity), eq("SUCCESS")))
        .thenReturn(Optional.of(buildEntity));
        final Pair<JobEntity, BuildEntity> pair = Pair.of(jobEntity, buildEntity);

        final ArgumentCaptor<List> jobsCapture = ArgumentCaptor.forClass(List.class);
        when(reportMapperHelper.maps(eq(Boolean.FALSE),jobsCapture.capture()))
        .thenReturn(reportListDto);

        final ReportListDto result = reportService.getProductReport(product,pageNo,pageSize);
        assertEquals(reportListDto, result);
        assertTrue(jobsCapture.getValue().contains(pair));
    }
    
    @Test
    void deleteObsoletedJobsTest() {
        final ReportService reportService = getReportService();
        final Set<String> productsFromProvider = new HashSet<>(Arrays.asList("Product1", "Product2"));
        
        final List<ProductEntity> allProducts = Arrays.asList(
                new ProductEntity("Product1"), 
                new ProductEntity("Product2"), 
                new ProductEntity("ObsoleteProduct"));
        
        final List<JobEntity> jobs = Arrays.asList(
                new JobEntity("Job2"), 
                new JobEntity("ObsoleteJob"));
        
        final List<Job> csvJobs = Arrays.asList(
                new Job("ObsoleteJob"));
        
        when(jenkinsProvider.getProducts()).thenReturn(productsFromProvider);
        when(jenkinsProvider.getJobs()).thenReturn(csvJobs);

        when(productRepository.findAll()).thenReturn(allProducts);
        when(jobRepository.findAll()).thenReturn(jobs);

        reportService.deleteObsoletedJobs();

        ArgumentCaptor<ProductEntity> productCaptor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).delete(productCaptor.capture());
    }


    private JobEntity getJobEntity() {
        return new JobEntity(getRandomString(),
                getRandomString(),
                getRandomString(),
                JobTypeEnum.PUBLISH,
                getRandomString(),
                null);
    }

    private ReportService getReportService() {
        return getReportService(getRandomString());
    }

    private ReportService getReportService(final String ungroupedProductName) {
        jobRepository = mock(JobRepository.class);
        productRepository = mock(ProductRepository.class);
        buildRepository = mock(BuildRepository.class);
        reportMapperHelper = mock(ReportMapperHelper.class);
        jenkinsProvider = mock(JenkinsProvider.class);
        
        return new ReportServiceImpl(
                jobRepository,
                productRepository,
                buildRepository,
                ungroupedProductName,
                reportMapperHelper,
                jenkinsProvider);
    }
}
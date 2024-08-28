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

import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomStageEntity;
import static com.ericsson.oss.ci.service.impl.TestHelper.getRandomString;
import static com.ericsson.oss.ci.service.impl.TestHelper.getStage;
import static com.ericsson.oss.ci.service.impl.TestHelper.getStageEntity;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.dto.configuration.Stage;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.entity.BlueoceanStageEntity;
import com.ericsson.oss.ci.domain.entity.BuildEntity;
import com.ericsson.oss.ci.domain.entity.JobEntity;
import com.ericsson.oss.ci.domain.entity.ProductEntity;
import com.ericsson.oss.ci.domain.entity.StageEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.ericsson.oss.ci.domain.repository.BlueoceanStageRepository;
import com.ericsson.oss.ci.domain.repository.BuildRepository;
import com.ericsson.oss.ci.domain.repository.JobRepository;
import com.ericsson.oss.ci.domain.repository.ProductRepository;
import com.ericsson.oss.ci.service.JobSaverService;
import com.ericsson.oss.ci.service.ParseResult;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;

import lombok.extern.slf4j.Slf4j;
@Slf4j
class JobSaverServiceImplTest {

    private JobSaverService jobSaverService;
    private  JobRepository jobRepository;
    private  BuildRepository buildRepository;
    private ProductRepository productRepository;
    private BlueoceanStageRepository blueoceanStageRepository;
    private RestTemplate restTemplate;
    private Stages stages;
    private String rule;
    private String rule2;
    private String appType;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        buildRepository = mock(BuildRepository.class);
        productRepository = mock(ProductRepository.class);
        blueoceanStageRepository = mock(BlueoceanStageRepository.class);
        restTemplate = mock(RestTemplate.class);
        rule = getRandomString();
        rule2 = getRandomString();
        appType = getRandomString();
        final Stage stage = getStage("Image", Set.of(rule, rule2));
        final Stage stage1 = getStage("Vulnerability Analysis", Set.of(rule));
        final Stage stage2 = getStage("Test", Set.of(rule));
        stages = TestHelper.getStages(List.of(
                TestHelper.getStandard(appType, List.of(stage, stage1, stage2), List.of(stage, stage1, stage2))
        ));
        jobSaverService = spy(new JobSaverServiceImpl(
                jobRepository,
                buildRepository,
                productRepository,
                stages,
                restTemplate, blueoceanStageRepository, appType, appType
        ));
    }

    @Test
    void saveBuildWithNewJob() throws IOException {
        final String url = getRandomString();
        final StageEntity hybridStage = getStageEntity("Hybrid", Set.of(rule, rule2));
        final StageEntity stage = getStageEntity("Image", Set.of(rule));
        final StageEntity stage1 = getStageEntity("Vulnerability Analysis", Set.of(rule));
        final StageEntity stage2 = getStageEntity("Test", Set.of());
        final StageEntity mockStage = getStageEntity("Vulnerability Analysis", Set.of(rule, rule2));

        final BuildEntity mockBuild = new BuildEntity();
        final BuildEntity mockBuild1 = null;
        final BuildEntity mockBuild2 = new BuildEntity();
        final BuildEntity mockBuild3 = new BuildEntity();
        mockBuild2.setStages(List.of(hybridStage));
        mockBuild.setStages(List.of(stage, stage2, mockStage));
        mockBuild3.setStages(List.of(stage, stage1));
        List<BuildEntity> mockBuilds = new ArrayList<>(Arrays.asList(mockBuild, mockBuild1, mockBuild2, mockBuild3));

        final JobWithDetails jobWithDetails = getJobWithDetails();
        final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        
        final Build build = getBuild();
        final ArgumentCaptor<BuildEntity> buildCaptor = ArgumentCaptor.forClass(BuildEntity.class);
        
        final ArgumentCaptor<BlueoceanStageEntity> blueoceanCaptor = ArgumentCaptor.forClass(BlueoceanStageEntity.class);
        when(blueoceanStageRepository.save(blueoceanCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BlueoceanStageEntity.class));
        List<BlueoceanStageEntity> mockBlueOceanStages = List.of(new BlueoceanStageEntity());
        doReturn(mockBlueOceanStages).when(jobSaverService).getBlueoceanStagesInfo(anyString(), anyString(), anyInt());
        doReturn("").when(jobSaverService).getSonarMetrics(eq(JobTypeEnum.PRE_CODE_REVIEW), anyString(), anyString());
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
        Job job = new Job(url, url, url, null, JobTypeEnum.PRE_CODE_REVIEW);
        JobEntity jb = new JobEntity();
        jb.setAppType(appType);
        jb.setType(JobTypeEnum.PRE_CODE_REVIEW);
        doReturn(jb).when(jobSaverService).getJobEntity(anyString(), eq(jobWithDetails), eq(job));

        when(buildRepository.save(buildCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BuildEntity.class));
        
        when(jobRepository.findFirstByJenkinsUrlAndNameOrderByCreatedDesc(eq(url), any()))
                .thenReturn(Optional.empty());
        when(jobWithDetails.getBuildByNumber(anyInt())).thenReturn(build);
        when(buildWithDetails.getDuration()).thenReturn(1L);
        when(build.details()).thenReturn(buildWithDetails);
        when(buildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);
        when(buildRepository.findFirst19ByJobAndBuildNoLessThanOrderByBuildNoDesc(any(), anyInt())).thenReturn(mockBuilds);

        jobSaverService.saveBuild(url, jobWithDetails, build, getParseResult(new ArrayList<>(Arrays.asList(stage, stage1, hybridStage))), job);

        verify(jobSaverService).getBlueoceanStagesInfo(stringCaptor.capture(), stringCaptor.capture(), intCaptor.capture());
        verify(buildRepository, times(1)).save(any());

        final BuildEntity buildEntity = buildCaptor.getValue();

        assertEquals(buildEntity.getBuildNo(), build.getNumber());
        assertEquals(buildEntity.getUrl(), build.getUrl());
        assertEquals(3,  buildEntity.getStages().size());
        final StageEntity stageEntity = buildEntity.getStages().get(0);
        assertEquals(stageEntity, stage);
    }
    
    @Test
    void testBlueoceanStagesInfo() {

    String testUrl = getRandomString();
    String testJob = getRandomString();
    String expectedUrl = testUrl + "blue/rest/organizations/jenkins/pipelines/"+ testJob +  "/runs/0/nodes/?limit=10000";
    
      when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<BlueoceanStageEntity>>() {})))
      .thenReturn(ResponseEntity.ok().body(Collections.emptyList()));
      
      jobSaverService.getBlueoceanStagesInfo(testUrl, testJob, 0);
    	
      verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<BlueoceanStageEntity>>() {}));
    }

    @Test
    void testBlueeoceanStagesInfoClientResponseException() {

    String testUrl = getRandomString();
    String testJob = getRandomString();
    String expectedUrl = testUrl + "blue/rest/organizations/jenkins/pipelines/"+ testJob +  "/runs/0/nodes/?limit=10000";
    
    RestClientResponseException mockException = mock(RestClientResponseException.class);
      when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<BlueoceanStageEntity>>() {})))
      .thenThrow(mockException);
      
      jobSaverService.getBlueoceanStagesInfo(testUrl, testJob, 0);
    	
      verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<BlueoceanStageEntity>>() {}));
    }
    
    @Test
    void testSonarReport() {
      String sampleResponse = "{\"component\": {\"measures\": [{\"metric\":\"quality_gate_details\", \"value\": \"{\\\"level\\\":\\\"OK\\\"}\"}, {\"metric\":\"new_bugs\", \"value\": \"0\"}]}}";
      ResponseEntity<String> expectedResponse = ResponseEntity.ok().body(sampleResponse);

      String expectedUrl = "https://test.com/api/measures/component?component=null&pullRequest=null&additionalFields=metrics&metricKeys=new_bugs,new_vulnerabilities,new_technical_debt,new_coverage,quality_gate_details";
      String sonarUrl = "https://test.com/dashboard?id=null&pullRequest=null";
      when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(), eq(String.class)))
      .thenReturn(expectedResponse);
      
      jobSaverService.getSonarMetrics(JobTypeEnum.PRE_CODE_REVIEW, sonarUrl, "SUCCESS");
    	
      verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), any(), eq(String.class));
    }
    
    @Test
    void testSonarReportClientErrorException() {

      String expectedUrl = "https://test.com/api/measures/component?component=null&pullRequest=null&additionalFields=metrics&metricKeys=new_bugs,new_vulnerabilities,new_technical_debt,new_coverage,quality_gate_details";
      String sonarUrl = "https://test.com/dashboard?id=null&pullRequest=null";
      when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.GET), any(), eq(String.class)))
      .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
      
      jobSaverService.getSonarMetrics(JobTypeEnum.PRE_CODE_REVIEW, sonarUrl, "SUCCESS");
    	
      verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.GET), any(), eq(String.class));
    }
    
    @Test
    void saveBuildWhenSonarReportNotFound() throws IOException {
    	final String url = getRandomString();
        final StageEntity stage = getRandomStageEntity();
        final StageEntity stage1 = getRandomStageEntity();
        final JobWithDetails jobWithDetails = getJobWithDetails();
        final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        
        final Build build = getBuild();

        final ArgumentCaptor<JobEntity> jobCaptor = ArgumentCaptor.forClass(JobEntity.class);
        final ArgumentCaptor<BuildEntity> buildCaptor = ArgumentCaptor.forClass(BuildEntity.class);
        when(buildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);
        when(jobRepository.save(jobCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, JobEntity.class));
        when(buildRepository.save(buildCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BuildEntity.class));
        when(jobRepository.findFirstByJenkinsUrlAndNameOrderByCreatedDesc(eq(url), any()))
                .thenReturn(Optional.empty());
        when(jobWithDetails.getBuildByNumber(anyInt())).thenReturn(build);
        when(build.details()).thenReturn(buildWithDetails);
        when(buildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);
        
        final ArgumentCaptor<BlueoceanStageEntity> blueoceanCaptor = ArgumentCaptor.forClass(BlueoceanStageEntity.class);
        when(blueoceanStageRepository.save(blueoceanCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BlueoceanStageEntity.class));
        List<BlueoceanStageEntity> mockBlueOceanStages = List.of(new BlueoceanStageEntity());
        doReturn(mockBlueOceanStages).when(jobSaverService).getBlueoceanStagesInfo(anyString(), anyString(), anyInt());
        doReturn("").when(jobSaverService).getSonarMetrics(eq(JobTypeEnum.PRE_CODE_REVIEW), anyString(), anyString());
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);

        Job job = new Job(url, url, url, null, JobTypeEnum.PRE_CODE_REVIEW);
        

        when(buildWithDetails.getDuration()).thenReturn(1L);
        jobSaverService.saveBuild(url, jobWithDetails, build, getParseResult(new ArrayList<>(Arrays.asList(stage, stage1))), job);
        verify(jobSaverService).getBlueoceanStagesInfo(stringCaptor.capture(), stringCaptor.capture(), intCaptor.capture());
        verify(buildRepository, times(1)).save(any());
        verify(jobRepository, times(1)).save(any());
        
        final BuildEntity buildEntity = buildCaptor.getValue();
        final JobEntity jobEntity = jobCaptor.getValue();
        assertEquals(jobEntity.getName(), jobWithDetails.getName());
        assertEquals(jobEntity.getUrl(), jobWithDetails.getUrl());
        assertEquals(jobEntity.getJenkinsUrl(), url);
        assertEquals(buildEntity.getBuildNo(), build.getNumber());
        assertEquals(buildEntity.getUrl(), build.getUrl());
        assertEquals(2,  buildEntity.getStages().size());
        final StageEntity stageEntity = buildEntity.getStages().get(0);
        assertEquals(stageEntity, stage);

    }


    @Test
    void saveBuildWithNewJobAndNewProduct() {
    	final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        final String url = getRandomString();
        final StageEntity stage = getRandomStageEntity();
        final StageEntity stage1 = getRandomStageEntity();
        final JobWithDetails jobWithDetails = getJobWithDetails();
        final Build build = getBuild();
        final String product = getRandomString();
        Job job = new Job(url, url, url, Set.of(product), JobTypeEnum.PRE_CODE_REVIEW);
        
        
        
        final ArgumentCaptor<JobEntity> jobCaptor = ArgumentCaptor.forClass(JobEntity.class);
        final ArgumentCaptor<BuildEntity> buildCaptor = ArgumentCaptor.forClass(BuildEntity.class);
        final ArgumentCaptor<BlueoceanStageEntity> blueoceanCaptor = ArgumentCaptor.forClass(BlueoceanStageEntity.class);
        when(buildWithDetails.getDuration()).thenReturn(1L);
        when(jobRepository.save(jobCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, JobEntity.class));
        when(buildRepository.save(buildCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BuildEntity.class));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, ProductEntity.class));
        when(jobRepository.findFirstByJenkinsUrlAndNameOrderByCreatedDesc(eq(url), any()))
                .thenReturn(Optional.empty());
        when(productRepository.findFirstByProduct(eq(product)))
                .thenReturn(Optional.empty());
        when(jobWithDetails.getBuildByNumber(anyInt())).thenReturn(build);
        try {
        	when(buildWithDetails.getDuration()).thenReturn(1L);
            when(buildWithDetails.getResult()).thenReturn(BuildResult.SUCCESS);
			when(build.details()).thenReturn(buildWithDetails);
			when(build.details().getDescription()).thenReturn(">1.130.0-h46b410d.tgz");
		} catch (IOException e) {
			log.error("Error during process job {}", e);
		}
        when(buildWithDetails.getResult()).thenReturn(BuildResult.FAILURE);
        
        final BuildEntity b = new BuildEntity();
        when(buildRepository.findFirstByJobAndBuildStatusOrderByBuildNoDesc(any(), any())).thenReturn(Optional.ofNullable(b));
        List<BlueoceanStageEntity> mockBlueOceanStages = List.of(new BlueoceanStageEntity());
        doReturn(mockBlueOceanStages).when(jobSaverService).getBlueoceanStagesInfo(anyString(), anyString(), anyInt());
        doReturn("").when(jobSaverService).getSonarMetrics(eq(JobTypeEnum.PRE_CODE_REVIEW), anyString(), anyString());
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
        when(blueoceanStageRepository.save(blueoceanCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BlueoceanStageEntity.class));

        jobSaverService.saveBuild(url, jobWithDetails, build, getParseResult(new ArrayList<>(Arrays.asList(stage, stage1))), job);

        verify(jobSaverService).getBlueoceanStagesInfo(stringCaptor.capture(), stringCaptor.capture(), intCaptor.capture());
        verify(buildRepository, times(2)).save(any());
        verify(jobRepository, times(1)).save(any());
        verify(productRepository, times(1)).save(any());

        final JobEntity jobEntity = jobCaptor.getValue();
        final BuildEntity buildEntity = buildCaptor.getValue();

        assertEquals(jobEntity.getName(), jobWithDetails.getName());
        assertEquals(jobEntity.getUrl(), jobWithDetails.getUrl());
        assertEquals(jobEntity.getJenkinsUrl(), url);
        assertEquals(jobEntity.getType(), job.getType());
        assertEquals(1, jobEntity.getProducts().size());
        assertEquals(product, jobEntity.getProducts().iterator().next().getProduct());
        assertEquals(buildEntity.getBuildNo(), build.getNumber());
        assertEquals(buildEntity.getUrl(), build.getUrl());
        assertEquals(2,  buildEntity.getStages().size());
        final StageEntity stageEntity = buildEntity.getStages().get(0);
        assertEquals(stageEntity, stage);
    }


    @Test
    void saveBuildWithExistsJob() {
    	final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        final String url = getRandomString();
        final StageEntity stage = getStageEntity("Image", Set.of());
        final StageEntity hybridStage = getStageEntity(getRandomString(), Set.of(rule));
        final JobWithDetails jobWithDetails = getJobWithDetails();
        final Build build = getBuild();
        final JobEntity jobEntity = new JobEntity();
        final ArgumentCaptor<BuildEntity> buildCaptor = ArgumentCaptor.forClass(BuildEntity.class);
        when(buildRepository.save(buildCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BuildEntity.class));
        when(jobRepository.findFirstByJenkinsUrlAndNameOrderByCreatedDesc(eq(url), any()))
                .thenReturn(Optional.of(jobEntity));
        when(jobWithDetails.getBuildByNumber(anyInt())).thenReturn(build);
        try {
			when(build.details()).thenReturn(buildWithDetails);
		} catch (IOException e) {
			log.error("Error during process job {}", e);
		}
        when(buildWithDetails.getDuration()).thenReturn(1L);
        when(buildWithDetails.getResult()).thenReturn(BuildResult.ABORTED);
        
        final ArgumentCaptor<BlueoceanStageEntity> blueoceanCaptor = ArgumentCaptor.forClass(BlueoceanStageEntity.class);
        when(blueoceanStageRepository.save(blueoceanCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0, BlueoceanStageEntity.class));
        List<BlueoceanStageEntity> mockBlueOceanStages = List.of(new BlueoceanStageEntity());
        doReturn(mockBlueOceanStages).when(jobSaverService).getBlueoceanStagesInfo(anyString(), anyString(), anyInt());
        doReturn("").when(jobSaverService).getSonarMetrics(eq(JobTypeEnum.PRE_CODE_REVIEW), anyString(), anyString());
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);

        Job job = new Job(url, url, url, null, JobTypeEnum.PRE_CODE_REVIEW);
        

        jobSaverService.saveBuild(url, jobWithDetails, build, getParseResult(new ArrayList<>(Arrays.asList(stage, hybridStage))), job);
        verify(jobSaverService).getBlueoceanStagesInfo(stringCaptor.capture(), stringCaptor.capture(), intCaptor.capture());

        verify(buildRepository, times(1)).save(any());
        verify(jobRepository, times(0)).save(any());

        final BuildEntity buildEntity = buildCaptor.getValue();

        assertEquals(buildEntity.getBuildNo(), build.getNumber());
        assertEquals(buildEntity.getUrl(), build.getUrl());
        assertEquals(jobEntity, buildEntity.getJob());
        assertEquals(2,  buildEntity.getStages().size());
        final StageEntity stageEntity = buildEntity.getStages().get(0);
        assertEquals(stageEntity, stage);
    }

    private JobWithDetails getJobWithDetails() {
        final JobWithDetails jobWithDetails = mock(JobWithDetails.class);
        final BuildWithDetails buildWithDetails = mock(BuildWithDetails.class);
        final Build build = mock(Build.class);
        when(jobWithDetails.getName()).thenReturn(getRandomString());
        when(jobWithDetails.getUrl()).thenReturn(getRandomString());
        when(jobWithDetails.getLastSuccessfulBuild()).thenReturn(build);
        when(jobWithDetails.getBuildByNumber(anyInt())).thenReturn(build);
        try {
            when(build.details()).thenReturn(buildWithDetails);
        } catch (IOException e) {
            log.error("Error during process job {}", e);
        }
        when(buildWithDetails.getDuration()).thenReturn(1L);
        return jobWithDetails;
    }

    @Test
    void updateProducts_empty() {
        final String url = getRandomString();
        final JobEntity jobEntity = new JobEntity();
        final JobWithDetails jobWithDetails = getJobWithDetails();
        final Build build = getBuild();

        when(jobRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, JobEntity.class));
        when(jobRepository.findFirstByJenkinsUrlAndNameOrderByCreatedDesc(eq(url), any()))
                .thenReturn(Optional.of(jobEntity));
        
        jobSaverService.saveBuild(url, jobWithDetails, build, getParseResult(emptyList()), new Job());

        verify(jobRepository, times(0)).save(any());
    }

    @Test
    void updateProducts_changeProducts() {
        final String newProduct = getRandomString();
        final String existsProduct = getRandomString();
        final String removedProduct = getRandomString();

        final JobEntity jobEntity = new JobEntity();
        jobEntity.setProducts(new HashSet<>());
        jobEntity.getProducts().add(getProduct(existsProduct));
        jobEntity.getProducts().add(getProduct(removedProduct));
        final JobWithDetails jobWithDetails = getJobWithDetails();
        final Build build = getBuild();
        final Job job = new Job();
        job.setProducts(Set.of(newProduct, existsProduct));

        final ArgumentCaptor<JobEntity> jobEntityArgumentCaptor = ArgumentCaptor.forClass(JobEntity.class);

        when(productRepository.findFirstByProduct(any()))
                .thenReturn(Optional.empty());
        when(productRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0, ProductEntity.class));
        when(jobRepository.save(jobEntityArgumentCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0, JobEntity.class));
        when(jobRepository.findFirstByJenkinsUrlAndNameOrderByCreatedDesc(any(), any()))
                .thenReturn(Optional.of(jobEntity));

        jobSaverService.saveBuild(getRandomString(), jobWithDetails, build, getParseResult(emptyList()), job);

        verify(jobRepository, times(1)).save(any());
        final JobEntity resultJob = jobEntityArgumentCaptor.getValue();
        assertNotNull( resultJob.getProducts());
        assertEquals(2, resultJob.getProducts().size());
        assertTrue(isContainsProduct(resultJob, newProduct));
        assertTrue(isContainsProduct(resultJob, existsProduct));
        assertFalse(isContainsProduct(resultJob, removedProduct));
    }

    private ProductEntity getProduct(final String product) {
        final ProductEntity productEntity = new ProductEntity();
        productEntity.setProduct(product);
        return productEntity;
    }

    private boolean isContainsProduct(final JobEntity job, final String product) {
        return job.getProducts().stream()
                .anyMatch(item -> StringUtils.equals(item.getProduct(), product));
    }
    

    private Build getBuild() {
        final Build build = mock(Build.class);
        when(build.getNumber()).thenReturn(1);
        when(build.getUrl()).thenReturn(getRandomString());
        return build;
    }

    private ParseResult getParseResult(final List<StageEntity> stages) {
    	String sonarUrl = "https://sample.host.com/dashboard?id=null&branch=null";
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
				return sonarUrl;
			}
        };
    }
}
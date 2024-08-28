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

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.collections4.ListUtils.unmodifiableList;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.oss.ci.domain.dto.configuration.Job;
import com.ericsson.oss.ci.domain.dto.configuration.Stage;
import com.ericsson.oss.ci.domain.dto.configuration.Stages;
import com.ericsson.oss.ci.domain.entity.BlueoceanStageEntity;
import com.ericsson.oss.ci.domain.entity.BuildEntity;
import com.ericsson.oss.ci.domain.entity.JobEntity;
import com.ericsson.oss.ci.domain.entity.ProductEntity;
import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import com.ericsson.oss.ci.domain.repository.BlueoceanStageRepository;
import com.ericsson.oss.ci.domain.repository.BuildRepository;
import com.ericsson.oss.ci.domain.repository.JobRepository;
import com.ericsson.oss.ci.domain.repository.ProductRepository;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ComponentDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.MetricDto;
import com.ericsson.oss.ci.service.JobSaverService;
import com.ericsson.oss.ci.service.ParseResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.JobWithDetails;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class JobSaverServiceImpl implements JobSaverService {
    private final JobRepository jobRepository;
	private final BuildRepository buildRepository;
    private final ProductRepository productRepository;
    private final BlueoceanStageRepository blueoceanStageRepository;
    private final Map<String, Map<JobTypeEnum, List<Stage>>> stagesByAppTypeAndJobType;
    private final RestTemplate restTemplate;
    private final String username;
    private final String password;

    public JobSaverServiceImpl(final JobRepository jobRepository, final BuildRepository buildRepository,
			final ProductRepository productRepository, final Stages stages, final RestTemplate restTemplate,
			final BlueoceanStageRepository blueoceanStageRepository, 
			@Value("${functional.user}") final String username,
			@Value("${functional.password}") final String password) {
		this.jobRepository = jobRepository;
		this.buildRepository = buildRepository;
		this.productRepository = productRepository;
		this.blueoceanStageRepository = blueoceanStageRepository;
		this.restTemplate=restTemplate;
		this.username = username;
		this.password = password;
		this.stagesByAppTypeAndJobType = emptyIfNull(stages.getStandards())
											.stream()
							                .collect(Collectors.toUnmodifiableMap(
							                        Stages.Standard::getAppType,
							                        standard -> Map.of(
							                                JobTypeEnum.PUBLISH, unmodifiableList(standard.getPublish()),
							                                JobTypeEnum.PRE_CODE_REVIEW, unmodifiableList(standard.getPreCodeReview())
							                )));
	}

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBuild(final String jenkinsUrl,
                          final JobWithDetails jenkinsJob,
                          final Build jenkinsBuild,
                          final ParseResult parseResult,
                          final Job job) {
        final JobEntity jobEntity = getJobEntity(jenkinsUrl, jenkinsJob, job);
        final BuildEntity build = new BuildEntity();
        build.setJob(jobEntity);
        build.setBuildNo(jenkinsBuild.getNumber());
        build.setUrl(jenkinsBuild.getUrl());
        build.setCbosVersion(parseResult.getCbosVersion());
        long ms =0;
        String buildDuration=null;
        Timestamp timestamp =null;
        BuildResult result = null;
        String helmVersion=null;
        List<BlueoceanStageEntity> blueOceanBuildInfo = new ArrayList<>();
        try {
        	 ms = jenkinsBuild.details().getDuration();
             if (ms == 0) {
                 log.info("Skipping build {} of job {} as its duration is 0", jenkinsBuild.getNumber(), job.getJobName());
                 return;
             }
             blueOceanBuildInfo = getBlueoceanStagesInfo(job.getJenkinsUrl(), job.getJobName(), jenkinsBuild.getNumber());
       	     result=jenkinsBuild.details().getResult();
             build.setBuildStatus(result.name());

             String sonarMetrics = getSonarMetrics(job.getType(), parseResult.getSonarUrl(), result.name());
             build.setSonarMetrics(sonarMetrics);

             Duration duration=Duration.ofMillis(ms);
             long minutesPart = duration.toMinutes();
             long secondsPart = duration.minusMinutes(minutesPart).getSeconds();
             buildDuration=minutesPart + " Mins " + secondsPart + " seconds";
             build.setBuildDuration(buildDuration);
             timestamp = new Timestamp(jenkinsBuild.details().getTimestamp());
             String description = jenkinsBuild.details().getDescription();
             String helmVersionRegex = "\\d+\\.\\d+\\.\\d+-[a-z0-9]+";
             Pattern p = Pattern.compile(helmVersionRegex);
             if(description!=null) {
             	Matcher matcher=p.matcher(description);
             	if(matcher!=null && matcher.find()) {
                 helmVersion = matcher.group(0);
             }
             }
             build.setSonarUrl(parseResult.getSonarUrl());
             build.setHelmVersion(helmVersion);
             build.setBuildCreatedDt(timestamp);
       } catch (Exception e3) {
           log.error("Error during saving job {}", job.getJobName(), e3);
       }
       parseResult.getStages().forEach(build::addStage);
       Pair<String, String> buildAndVAalignments = getAlignmentValues(jobEntity, build);

       if(build.getBuildStatus() != null) {
           if(build.getBuildStatus().equals("SUCCESS")) {
               build.setBuildAlignment(buildAndVAalignments.getKey());
               build.setVaAlignment(buildAndVAalignments.getValue());
           }else{
                  buildRepository.findFirstByJobAndBuildStatusOrderByBuildNoDesc(jobEntity, "SUCCESS")
                                 .ifPresent(latestBuild -> {
                                                latestBuild.setBuildAlignment(buildAndVAalignments.getKey());
                                                latestBuild.setVaAlignment(buildAndVAalignments.getValue());
                                                buildRepository.save(latestBuild);
                                   });
           }
       }
       buildRepository.save(build);
       emptyIfNull(blueOceanBuildInfo).forEach(e -> {
    	   e.setBuild(build);
    	   blueoceanStageRepository.save(e);
       });
       
    }
    
    @Override
    public List<BlueoceanStageEntity> getBlueoceanStagesInfo(String jenkinsUrl, String jobName, int buildNo) {
    	
    	 String blueoceanBuildUrl = jenkinsUrl
         		+ "blue/rest/organizations/jenkins/pipelines/" + jobName
         		+ "/runs/" + buildNo
         		+ "/nodes/?limit=10000";
    	
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBasicAuth(username, password);
        HttpEntity<String> entity = new HttpEntity<>(requestHeaders);

        try {
            ResponseEntity<List<BlueoceanStageEntity>> response = restTemplate.exchange(
            		blueoceanBuildUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<BlueoceanStageEntity>>() {}
            );
            return response.getBody();
        } catch (RestClientResponseException e) {
        	log.error("error occured while fetching blueocean build info : {}", e.getMessage(), e);
            return Collections.emptyList(); 
        }
    }

    @Override
    public JobEntity getJobEntity(final String jenkinsUrl, final JobWithDetails jenkinsJob, final Job job) {
        return jobRepository.findFirstByJenkinsUrlAndNameOrderByCreatedDesc(jenkinsUrl, jenkinsJob.getName())
                .map(jobEntity -> updateProducts(jobEntity, job))
                .orElseGet(() -> createNewJob(jenkinsUrl, jenkinsJob, job));
    }

    @Override
	public String getSonarMetrics(final JobTypeEnum jobType, final String sonarUrl, final String buildStatus) {

		if(sonarUrl == null || !buildStatus.equals("SUCCESS")) {
		    return null;
		}
		String additionalFields = "metrics";
        String metricKeysPCR = "new_bugs,new_vulnerabilities,new_technical_debt,new_coverage,quality_gate_details";
        String metricKeysPublish = "bugs,vulnerabilities,sqale_index,coverage,quality_gate_details";
        String username = "mmtadm";
        String password = "Er4mrqvBybp7F6MWtCtGn9Fg";

        HttpHeaders headers = getHttpHeaders(username, password);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        String statusCode = "";
		try {
			URI uri = new URI(sonarUrl);
			String query = uri.getQuery().replace("id=", "component=");
			URI newUri = new URI(uri.getScheme(), uri.getHost(), "/api/measures/component", query, null);

			String url = UriComponentsBuilder.fromUriString(newUri.toString())
							.queryParam("additionalFields", additionalFields)
						    .queryParam("metricKeys", jobType.equals(JobTypeEnum.PUBLISH) ? metricKeysPublish : metricKeysPCR)
							.toUriString();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            statusCode = Integer.toString(response.getStatusCodeValue());

            ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> o = mapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			final ComponentDto componentDto = mapper.convertValue(o.get("component"), ComponentDto.class);
			return fetchSonarQualityGateStatus(mapper, componentDto, jobType) + ";statusCode:" + statusCode;

		}
		 catch(HttpClientErrorException e) {
				log.error("error occured for sonarQube with status code: {}, sonarUrl: {}", e.getRawStatusCode(), sonarUrl, e);
				return ";statusCode:" + Integer.toString(e.getRawStatusCode());
		}
		catch (Exception e) {
			log.error("error occured for sonarQube with error message: {}", e.getMessage(), e);
			return ";error:"+e.getMessage();
		}
	}

	private HttpHeaders getHttpHeaders(String username, String password) {
        String credentials = username + ":" + password;
        String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + base64Credentials);
        headers.set("Content-Type", "application/json");
        return headers;
	}

	String fetchSonarQualityGateStatus(ObjectMapper mapper, final ComponentDto componentDto, final JobTypeEnum jobType){

		Map<String, String> metricsMap = new HashMap<>();
		metricsMap.put("bugs", "new_bugs");
        metricsMap.put("new_bugs", "new_bugs");
        metricsMap.put("vulnerabilities", "new_vulnerabilities");
        metricsMap.put("new_vulnerabilities", "new_vulnerabilities");
        metricsMap.put("sqale_index", "new_technical_debt");
        metricsMap.put("new_technical_debt", "new_technical_debt");
        metricsMap.put("coverage", "new_coverage");
        metricsMap.put("new_coverage", "new_coverage");

        Map<String, String> resultMap = new HashMap<>();

	    for(Iterator<MetricDto> it = componentDto.getMeasures().iterator(); it.hasNext(); ) {
	        MetricDto metric = it.next();
	        processMetric(metric, mapper, jobType, metricsMap, resultMap);
	        it.remove();
	    }
		return resultMap.values().stream().collect(Collectors.joining());
	}

	private void processMetric(MetricDto metric, ObjectMapper mapper, JobTypeEnum jobType, Map<String, String> metricsMap, Map<String, String> resultMap) {
	    String qualityGate = "quality_gate_details";
	    if(metric.getMetric() != null) {
	        if(metric.getMetric().equals(qualityGate)) {
	            processQualityGateMetric(metric, mapper, resultMap);
	        } else {
	            processOtherMetric(metric, jobType, metricsMap, resultMap);
	        }
	    }
	}

	private void processQualityGateMetric(MetricDto metric, ObjectMapper mapper, Map<String, String> resultMap) {
	    try {
            resultMap.put(metric.getMetric(), ";" + metric.getMetric() + ":" + mapper.readTree(metric.getValue()).path("level").asText());
	    } catch (JsonProcessingException e) {
	        log.error("Error while parsing the sonar response: {}", e.getMessage(), e);
	        resultMap.put(metric.getMetric(), ";" + metric.getMetric() + ":NA");
	    }
	}

	private void processOtherMetric(MetricDto metric, JobTypeEnum jobType, Map<String, String> metricsMap, Map<String, String> resultMap) {
	    String value = jobType.equals(JobTypeEnum.PRE_CODE_REVIEW) && metric.getPeriod() != null ? metric.getPeriod().getValue() : metric.getValue();
	    String key = metricsMap.getOrDefault(metric.getMetric(), "");
	    if (!key.isEmpty()) {
            resultMap.put(key, ";" + key + ":" + value);
	    }
	}

    private Pair<String, String> getAlignmentValues(JobEntity jobEntity, BuildEntity buildEntity){

        DecimalFormat df = new DecimalFormat("#.##");
        final Map<String, Set<String>> stagesWithRules = getStagesWithRules(jobEntity.getAppType(), jobEntity.getType());
        final int totalStages = stagesWithRules.keySet().size();
        final Integer buildNo = buildEntity.getBuildNo();
        List<BuildEntity> last20Builds = buildRepository.findFirst19ByJobAndBuildNoLessThanOrderByBuildNoDesc(jobEntity, buildNo);
        last20Builds.add(0, buildEntity);

        List<Double> stageAlignments = last20Builds.stream().filter(build -> build != null && build.getStages() != null)
                                       .map(build -> build.getStages().stream()
                                    		   .filter(stage -> stagesWithRules.get(stage.getName()) != null)
                                    		   .filter( stage -> stage.getRules() != null)
                                    		   .count() * 100.0 / totalStages)
                                       .collect(Collectors.toList());

        Double alignPercFor20Builds =  stageAlignments.stream().reduce(0.0, Double::sum) / stageAlignments.size() ;
        Boolean stageAlignmentSymbol = calculateBuildAlignmentValue(stageAlignments);
        String booleanString = stageAlignmentSymbol == null ? "null" : stageAlignmentSymbol.toString();
        String leftValue = df.format(alignPercFor20Builds) +";"+ booleanString;
        Map<String, Pair<String, String>> stagesAlignments = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : stagesWithRules.entrySet()) {
            String stageName = entry.getKey();
            Set<String> standardStageRules = entry.getValue();

            List<Integer> stagesCountList = last20Builds.stream()
                .filter(build -> build != null && build.getStages() != null)
                .flatMap(build -> build.getStages().stream()
                    .filter(stage -> stage.getName().equals(stageName))
                    .filter(stageEntity -> Objects.nonNull(stageEntity.getRules()))
                    .map(stageEntity -> stageEntity.getRules().size()))
                .collect(Collectors.toList());

            Integer stageAlignmentDiff = calculateStagealignmentValue(stagesCountList);
            Double stageAlignPercFor20Builds = 0.0;

            if (standardStageRules != null && !standardStageRules.isEmpty() && !stagesCountList.isEmpty()) {
                int standardRulesSize = standardStageRules.size();
                int sum = stagesCountList.stream().mapToInt(Integer::intValue).sum();
                stageAlignPercFor20Builds = sum * 100.0 / (standardRulesSize * stagesCountList.size());
            }
            String leftValue1 = df.format(stageAlignPercFor20Builds);
            String rightValue1 = stageAlignmentDiff.toString();
            stagesAlignments.put(stageName, Pair.of(leftValue1, rightValue1));
        }
        String calculateStagesRulesAlignmentsString = mapToString(stagesAlignments);
        return Pair.of(leftValue, calculateStagesRulesAlignmentsString);

    }
    
    public String mapToString(Map<String, Pair<String, String>> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Pair<String, String>> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue().getLeft()).append(";").append(entry.getValue().getRight()).append("\n");
        }
        return sb.toString();
    }

	private Boolean calculateBuildAlignmentValue(List<Double> list) {
	    int listSize = list.size();
	    Boolean result = null;
	    for(int i=0; i<listSize-1; i++) {
	        Double curr = list.get(i);
	        Double next = list.get(i+1);
	        if(!curr.equals(next)) {
	            result = curr > next;
	            break;
	        }
	    }
	    return result;
	}

	private Integer calculateStagealignmentValue(List<Integer> list) {
	    int listSize = list.size();
	    Integer result = 0;
	    for(int i=0; i<listSize-1; i++) {
	        Integer curr = list.get(i);
	        Integer next = list.get(i+1);
	        if(!curr.equals(next)) {
	            result = curr - next;
	            break;
	        }
	    }
	    return result;
	}

	 private Map<String, Set<String>> getStagesWithRules(final String appType, final JobTypeEnum jobType){
        return Optional.ofNullable(appType)
                .map(stagesByAppTypeAndJobType::get)
                .map(map -> map.get(jobType))
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toUnmodifiableMap(Stage::getName, Stage::getRules));
    }

    private JobEntity updateProducts(final JobEntity jobEntity, final Job job) {
        if (isEmpty(jobEntity.getProducts()) && isEmpty(job.getProducts())) {
            return jobEntity;
        }
        final Set<String> resultProductsNames = new HashSet<>();
        if (jobEntity.getProducts() == null) {
            jobEntity.setProducts(new HashSet<>());
        }
        emptyIfNull(job.getProducts())
                .forEach(product -> {
                    if (!isContainsProduct(jobEntity, product)) {
                        jobEntity.getProducts().add(getProductEntity(product));
                    }
                    resultProductsNames.add(product);
                });
        jobEntity.getProducts().removeIf(item -> !resultProductsNames.contains(item.getProduct()));
        return jobRepository.save(jobEntity);
    }

    private boolean isContainsProduct(final JobEntity job, final String product) {
        return job.getProducts().stream()
                .anyMatch(item -> StringUtils.equals(item.getProduct(), product));
    }

    private JobEntity createNewJob(final String jenkinsUrl, final JobWithDetails jenkinsJob, final Job job) {
        final JobEntity newJob = JobEntity.builder()
                .name(jenkinsJob.getName())
                .url(jenkinsJob.getUrl())
                .jenkinsUrl(jenkinsUrl)
                .type(job.getType())
                .appType(job.getAppType())
                .products(getProducts(job.getProducts()))
                .build();
        return jobRepository.save(newJob);
    }

    private Set<ProductEntity> getProducts(Set<String> products) {	
        return emptyIfNull(products).stream()
                .map(this::getProductEntity)
                .collect(Collectors.toSet());
    }

    private ProductEntity getProductEntity(final String product) {
        return productRepository.findFirstByProduct(product)
                .orElseGet(() -> {
                    final ProductEntity productEntity = new ProductEntity();
                    productEntity.setProduct(product);
                    return productRepository.save(productEntity);
                });
    }
}

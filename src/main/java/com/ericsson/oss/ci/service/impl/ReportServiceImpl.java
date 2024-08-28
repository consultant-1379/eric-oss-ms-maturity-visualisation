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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com.ericsson.oss.ci.domain.entity.BaseEntity;
import com.ericsson.oss.ci.domain.entity.BuildEntity;
import com.ericsson.oss.ci.domain.entity.JobEntity;
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

@Service
public class ReportServiceImpl implements ReportService {

    private final JobRepository jobRepository;
    private final ProductRepository productRepository;
    private final BuildRepository buildRepository;
    private final String ungroupedProductName;
    private final ReportMapperHelper reportMapperHelper;
    private final JenkinsProvider jenkinsProvider;
    private Boolean nextSlice = false;
    
    public ReportServiceImpl(final JobRepository jobRepository,
                             final ProductRepository productRepository,
                             final BuildRepository buildRepository,
                             @Value("${ungrouped.product.name}")
                             final String ungroupedProductName,
                             final ReportMapperHelper reportMapperHelper,
                             final JenkinsProvider jenkinsProvider) {
        this.jobRepository = jobRepository;
        this.productRepository = productRepository;
        this.buildRepository = buildRepository;
        this.ungroupedProductName = ungroupedProductName;
        this.reportMapperHelper = reportMapperHelper;
        this.jenkinsProvider = jenkinsProvider;
    }

    @Override
    public ProductResponseDto getProducts() {
    	final Set<String> lastWeekAddedJobs = jobRepository.getJobs();
        final List<String>  productsWithTotalJobs = productRepository.getJobsByEachProduct();
        List<List<String>> productsWithTypes = jobRepository.getAppTypeAndTypes();
        
        Map<String, List<MetaData>> appTypeMap = new HashMap<>();
        productsWithTypes.forEach(entry -> {
        	String product = entry.get(0);
            MetaData m = MetaData.builder()
                        .section(entry.get(1))
                        .totalJobs(entry.get(2))
                        .build();
            if(appTypeMap.get(product) == null) {
                List<MetaData> l = new ArrayList<>();
                appTypeMap.put(product, l);
            }
            appTypeMap.get(product).add(m);
        });


        List<ProductWiseMetadata> metadata = productsWithTotalJobs.stream()
        .map(p->p.split(","))
        .map(entry -> 
            {
                String product = entry[0];
                List<MetaData> metaData = appTypeMap.get(product);
                return ProductWiseMetadata.builder()
                .product(product)
                .metaData(metaData)
                .totalJobs(entry[1])
                .build();
            }
        )
        .sorted((p1, p2) -> p1.getProduct().compareTo(p2.getProduct()))
        .collect(Collectors.toList());
        
        return ProductResponseDto.builder()
        		.jobNotification(lastWeekAddedJobs)
        		.productWiseMetadata(metadata)
        		.build();
    }
    
    @Override
    public ReportsDto getProductReports(final String product,final String appType,final String type, final Integer pageNo, final Integer pageSize) {
    	Pageable pageable = PageRequest.of(pageNo,pageSize);
        final Collection<JobEntity> jobEntities = ungroupedProductName.equals(product)
                ? jobRepository.findByProductsEmpty()
                : productRepository.findFirstByProduct(product)
                .map(BaseEntity::getId)
                .map(productId-> {
                	Slice<JobEntity> jobsSlice = jobRepository.findByProductId(productId,appType,type,pageable);
                	nextSlice = jobsSlice.hasNext();
                	return jobsSlice.getContent();
                } )
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.collectingAndThen(Collectors.toList(), CollectionUtils::emptyIfNull));
        
        if (jobEntities.isEmpty()) {
            return ReportsDto.builder()
                    .reports(new ReportDto())
                    .unknownJobNames(Collections.emptyList())
                    .nextSlice(false)
                    .build();
        }
        final List<Pair<JobEntity, BuildEntity>> jobsWithLastBuilds = jobEntities.stream()
        		.map(job -> Pair.of(job, buildRepository.findFirstByJobAndBuildStatusOrderByBuildNoDesc(job, "SUCCESS")))
                .filter(pair -> pair.getValue().isPresent())
                .map(pair -> Pair.of(pair.getKey(), pair.getValue().get()))
                .collect(Collectors.toList());
        return reportMapperHelper.map(nextSlice, jobsWithLastBuilds);
    }
    
    @Override
    public void deleteOldBuilds() {
        jobRepository.findAll().stream().forEach(job -> {
            List<BuildEntity> builds = buildRepository.findFirst20ByJobOrderByBuildNoDesc(job);
            List<Integer> buildNos = builds.stream().map(build -> build.getBuildNo()).collect(Collectors.toList());
            buildRepository.deleteByJobAndBuildNoNotIn(job, buildNos);
        });
    }

	@Override
	public ReportListDto getProductReport(String product, Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo,pageSize);
        final Collection<JobEntity> jobEntities = ungroupedProductName.equals(product)
                ? jobRepository.findByProductsEmpty()
                : productRepository.findFirstByProduct(product)
                .map(BaseEntity::getId)
                .map(productId-> {
                	Slice<JobEntity> jobsSlice = jobRepository.findByProduct(productId,pageable);
                	nextSlice = jobsSlice.hasNext();
                	return jobsSlice.getContent();
                } )
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.collectingAndThen(Collectors.toList(), CollectionUtils::emptyIfNull));

        if (jobEntities.isEmpty()) {
            return ReportListDto.builder()
                    .reports(Collections.emptyList())
                    .unknownJobNames(Collections.emptyList())
                    .nextSlice(false)
                    .build();
        }
        final List<Pair<JobEntity, BuildEntity>> jobsWithLastBuilds = jobEntities.stream()
        		.map(job -> Pair.of(job, buildRepository.findFirstByJobAndBuildStatusOrderByBuildNoDesc(job, "SUCCESS")))
                .filter(pair -> pair.getValue().isPresent())
                .map(pair -> Pair.of(pair.getKey(), pair.getValue().get()))
                .collect(Collectors.toList());
        return reportMapperHelper.maps(nextSlice, jobsWithLastBuilds);
	}
	
	@Override
    public void deleteObsoletedJobs() {
		
		Set<String> products = jenkinsProvider.getProducts();
		Set<String> jobNamesFromCsv = jenkinsProvider
				.getJobs()
				.stream()
				.map( job -> job.getJobName()).collect(Collectors.toSet());
		
		productRepository.findAll().stream()
		.filter(product -> !products.contains(product.getProduct()))
		.forEach(productRepository::delete);
		
        jobRepository.findAll().stream()
        .filter(job -> !jobNamesFromCsv.contains(job.getName()))
        .forEach(jobRepository::delete);
        
        jobRepository.deleteJobProductRelationByJob();
        jobRepository.deleteJobProductRelationByProduct();
        
    }
	
}
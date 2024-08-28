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
package com.ericsson.oss.ci.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ericsson.oss.ci.domain.entity.JobEntity;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {
    Optional<JobEntity> findFirstByJenkinsUrlAndNameOrderByCreatedDesc(String jenkinsUrl, String jobName);
    
    @Query(value="select * from JOB j inner join JOB_PRODUCT jb on j.ID=jb.JOB_ID inner join PRODUCT p on jb.PRODUCT_ID=p.ID where p.ID=:productId and j.app_type= :appType and j.type= :type",nativeQuery=true)
    Slice<JobEntity> findByProductId(@Param("productId")Long productId,@Param("appType")String appType,@Param("type")String type, Pageable pageable);
  
    Collection<JobEntity> findByProductsEmpty();

    boolean existsByProductsEmpty();
    
    @Query(value="select name from job where created >= current_date - 7", nativeQuery=true)
    Set<String> getJobs();
    
    @Query(value="select p.product,concat(j.app_type, ' ', j.type),count(*) from job j inner join job_product jp ON j.id = jp.job_id inner join PRODUCT p on jp.PRODUCT_ID=p.ID where j.id in (select distinct job_id from build where build_status = 'SUCCESS') group by p.product,j.app_type,j.type order by p.product;", nativeQuery=true)
    List<List<String>> getAppTypeAndTypes();
    
    @Query(value="select product from product where id in (select product_id from job_product where job_id= :jobId)",nativeQuery=true)
    List<String> findProductsByJob(@Param("jobId")Long jobId);

    @Query(value="select * from JOB j inner join JOB_PRODUCT jb on j.ID=jb.JOB_ID inner join PRODUCT p on jb.PRODUCT_ID=p.ID where p.ID=:productId",nativeQuery=true)
    Slice<JobEntity> findByProduct(@Param("productId")Long productId, Pageable pageable);
      
    @Transactional
    @Modifying
    @Query(value="delete from job_product where job_id not in (select id from job )",nativeQuery=true)
    void deleteJobProductRelationByJob();
    
    @Transactional
    @Modifying
    @Query(value="delete from job_product where product_id not in (select id from product )",nativeQuery=true)
    void deleteJobProductRelationByProduct();
}

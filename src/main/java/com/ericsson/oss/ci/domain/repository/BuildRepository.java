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

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.ci.domain.entity.BuildEntity;
import com.ericsson.oss.ci.domain.entity.JobEntity;

@Repository
public interface BuildRepository extends CrudRepository<BuildEntity, Long> {
    boolean existsByJobJenkinsUrlAndJobNameAndBuildNo(String jenkinsUrl, String jobName, int buildNo);
    Optional<BuildEntity> findFirstByJobAndBuildStatusOrderByBuildNoDesc(JobEntity job, String buildStatus);
    @Query(value = "select max(cbosVersion) from BuildEntity B where B.buildStatus='SUCCESS'")
    String getMaxVersion();
    
    List<BuildEntity> findFirst20ByJobOrderByBuildNoDesc(JobEntity job);
    
    List<BuildEntity> findFirst19ByJobAndBuildNoLessThanOrderByBuildNoDesc(JobEntity job, Integer buildNo);

    
    List<BuildEntity> findFirst19ByJobOrderByBuildNoDesc(JobEntity job);

    @Modifying
    @Transactional
    void deleteByJobAndBuildNoNotIn(JobEntity job,List<Integer>  builds);	
}

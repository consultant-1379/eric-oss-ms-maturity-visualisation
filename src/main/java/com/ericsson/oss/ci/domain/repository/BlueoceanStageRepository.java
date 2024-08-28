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

import org.springframework.data.jpa.repository.JpaRepository;

import com.ericsson.oss.ci.domain.entity.BlueoceanStageEntity;
import com.ericsson.oss.ci.domain.entity.BuildEntity;

public interface BlueoceanStageRepository extends JpaRepository<BlueoceanStageEntity, Long> {
	
	List<BlueoceanStageEntity> findByBuild(BuildEntity build);

}

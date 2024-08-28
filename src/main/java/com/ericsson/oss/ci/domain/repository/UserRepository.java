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

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.ci.domain.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

	@Query(value = "SELECT COUNT(DISTINCT u.user_id) FROM users u WHERE DATE(u.created) = CURRENT_DATE", nativeQuery = true)
	 Long countDistinctUsersCreatedToday();

   @Query(value = "SELECT DATE(u.created) as creationDate, COUNT(DISTINCT u.user_id) as userCount " +
           "FROM users u " +
           "WHERE u.created <= :endDate " +
           "GROUP BY DATE(u.created)", nativeQuery = true)
   List<Object[]> countDistinctUsersByDayUntilDate(@Param("endDate") LocalDate endDate);

}

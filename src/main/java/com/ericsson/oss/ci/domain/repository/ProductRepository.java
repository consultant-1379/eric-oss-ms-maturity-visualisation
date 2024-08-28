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
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.ci.domain.entity.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findFirstByProduct(final String product);

    @Query(value = "select distinct P.product from ProductEntity P")
    Set<String> findDistinctProducts();
    
    @Query(value="SELECT p.product, COUNT(j.product_id) FROM product p LEFT JOIN job_product j ON p.id = j.product_id GROUP BY p.product",nativeQuery=true)
    List<String> getJobsByEachProduct();
}

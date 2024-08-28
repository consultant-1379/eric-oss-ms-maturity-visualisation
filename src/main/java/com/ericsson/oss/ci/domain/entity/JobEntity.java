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
package com.ericsson.oss.ci.domain.entity;

import com.ericsson.oss.ci.domain.enumeration.JobTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "JOB")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEntity extends BaseEntity{

    private static final long serialVersionUID = 8994592898024525456L;
    @Column(name = "NAME")
    private String name;
    @Column(name = "URL")
    private String url;
    @Column(name = "JENKINS_URL")
    private String jenkinsUrl;
    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private JobTypeEnum type;
    @Column(name = "APP_TYPE")
    private String appType;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "JOB_PRODUCT",
            joinColumns = {@JoinColumn(name = "JOB_ID")},
            inverseJoinColumns = {@JoinColumn(name = "PRODUCT_ID")}
    )
    private Set<ProductEntity> products;
    
    public JobEntity(String jobName) {
    	this.name = jobName;
    }
}

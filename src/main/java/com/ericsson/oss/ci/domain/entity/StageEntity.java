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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.ericsson.oss.ci.domain.converter.SetToStringConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(exclude = "build")
@Entity
@Table(name = "STAGE")
@NoArgsConstructor
public class StageEntity extends BaseEntity {

	public StageEntity(String name, Set<String> rules) {
		this.name = name;
		this.rules = new HashSet<>(rules);
	}

    private static final long serialVersionUID = 3179603953204429036L;
    @Fetch(FetchMode.JOIN)
    @ManyToOne
    @JoinColumn(name = "BUILD_ID")
    private BuildEntity build;

    @Column(name = "NAME")
    private String name;

    @Column(name = "RULES", length = 2000)
    @Convert(converter = SetToStringConverter.class)
    private Set<String> rules;
}

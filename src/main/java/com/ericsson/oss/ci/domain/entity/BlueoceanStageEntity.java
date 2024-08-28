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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.StageResult;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.StageState;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.StageType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(exclude = "build")
@Entity
@Table(name = "BLUEOCEAN_STAGE")
@NoArgsConstructor
public class BlueoceanStageEntity extends BaseEntity{
	
	private static final long serialVersionUID = -7218053018946965791L;

	@Column(name = "DISPLAY_NAME")
	private String displayName;

	@Column(name = "DURATION_IN_MILLIS")
	private Long durationInMillis;

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	
	private StageType type;

	@Column(name = "RESULT")
	@Enumerated(EnumType.STRING)
	private StageResult result;

	@Column(name = "STATE")
	@Enumerated(EnumType.STRING)
	private StageState state;
	
    @ManyToOne
    @JoinColumn(name = "BUILD_ID")
    private BuildEntity build;
    
}


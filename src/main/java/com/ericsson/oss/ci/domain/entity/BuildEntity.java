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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "BUILD")
public class BuildEntity extends BaseEntity{

    private static final long serialVersionUID = -7591060552878087296L;
    @Fetch(FetchMode.SELECT)
    @ManyToOne
    @JoinColumn(name = "JOB_ID")
    private JobEntity job;

    @Column(name = "BUILD_NO")
    private int buildNo;

    @Column(name = "URL")
    private String url;

    @Column(name = "CBOS_VERSION")
    private String cbosVersion;

    @Column(name = "BUILD_STATUS")
    private String buildStatus;

    @Column(name = "BUILD_CREATED_DT")
    private Timestamp buildCreatedDt;

    @Column(name = "BUILD_DURATION")
    private String buildDuration;

    @Column(name = "HELM_VERSION")
    private String helmVersion;

    @Column(name = "SONAR_URL")
    private String sonarUrl;

    @Column(name = "SONAR_METRICS")
    private String sonarMetrics;

    @Column(name = "BUILD_ALIGNMENT")
    private String buildAlignment;

    @Column(name = "VA_ALIGNMENT")
    private String vaAlignment;

    @Fetch(FetchMode.JOIN)
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<StageEntity> stages;

    public boolean addStage(final StageEntity stage) {
        if (stage == null) {
            return false;
        }
        if (stages == null) {
            stages = new ArrayList<>();
        }
        stage.setBuild(this);
        return stages.add(stage);
    }
}

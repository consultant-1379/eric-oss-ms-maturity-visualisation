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
package com.ericsson.oss.ci.domain.dto.configuration;

import com.ericsson.oss.ci.configuration.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource(value = "classpath:config/stages.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "stages")
@Data
public class Stages {
    private List<Standard> standards;

    @Data
    public static class Standard {
        private String appType;
        private List<Stage> publish;
        private List<Stage> preCodeReview;
    }


}

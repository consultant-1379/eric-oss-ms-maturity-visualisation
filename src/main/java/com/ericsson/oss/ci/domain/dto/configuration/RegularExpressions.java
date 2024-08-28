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

import java.util.Set;

@Configuration
@PropertySource(value = "classpath:config/regular-expressions.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "config")
@Data
public class RegularExpressions {
	private String stageNameRegexp;
	private String skippedStageNameRegexp;
    private String cbosVersionRegexp;
    private String sonarRegexp;
    private String ruleAndTaskRegex;
    private String dockerRunRuleRegex;
    private String sonarAndK8sRegex;
}

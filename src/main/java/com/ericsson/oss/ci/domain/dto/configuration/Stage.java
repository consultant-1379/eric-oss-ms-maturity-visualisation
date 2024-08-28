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

import lombok.Data;

import java.util.Set;

import static org.apache.commons.collections4.SetUtils.emptyIfNull;

@Data
public class Stage {
    private String name;
    private Set<String> rules;

    public Set<String> getRules() {
        return emptyIfNull(rules);
    }
}

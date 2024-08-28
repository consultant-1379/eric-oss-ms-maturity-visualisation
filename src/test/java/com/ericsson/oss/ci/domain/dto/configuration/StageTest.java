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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StageTest {

    @Test
    void testGetNullRules() {
        final Stage stage = new Stage();
        assertNotNull(stage.getRules());
    }

    @Test
    void testGetNullRulesSetNull() {
        final Stage stage = new Stage();
        stage.setRules(null);
        assertNotNull(stage.getRules());
    }

}
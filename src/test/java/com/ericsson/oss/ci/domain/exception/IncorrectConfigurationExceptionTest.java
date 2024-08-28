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
package com.ericsson.oss.ci.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncorrectConfigurationExceptionTest {

    @Test
    void IncorrectConfigurationExceptionTest() {
        final String message = UUID.randomUUID().toString();
        final IncorrectConfigurationException exception = new IncorrectConfigurationException(message);
        assertEquals(message, exception.getMessage());
    }

}
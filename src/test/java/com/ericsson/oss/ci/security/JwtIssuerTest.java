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
package com.ericsson.oss.ci.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtIssuerTest {
	 private JwtProperties jwtProperties;
    private JwtIssuer jwtIssuer;

    @BeforeEach
    void setUp() {
        jwtProperties = mock(JwtProperties.class);
        jwtIssuer = new JwtIssuer(jwtProperties);
    }
    
    @Test
    void issue_Successful() {
        when(jwtProperties.getSecretKey()).thenReturn("testSecretKey");
        String userDn = "testUserDn";
        String username = "testUsername";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        String jwtToken = jwtIssuer.issue(userDn, username, expiresAt);

        assertEquals(3, jwtToken.split("\\.").length);
    }

    @Test
    void issue_NullProperties() {
        when(jwtProperties.getSecretKey()).thenReturn(null);
        String userDn = "testUserDn";
        String username = "testUsername";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        assertThrows(IllegalArgumentException.class, () -> jwtIssuer.issue(userDn, username, expiresAt));
    }


}

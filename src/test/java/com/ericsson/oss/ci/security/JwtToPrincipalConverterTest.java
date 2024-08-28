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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtToPrincipalConverterTest {

	private final JwtToPrincipalConverter jwtToPrincipalConverter = new JwtToPrincipalConverter();
	private final DecodedJWT decodedJWT = mock(DecodedJWT.class);
	 
	@BeforeEach
    void setUp() {
        when(decodedJWT.getSubject()).thenReturn("mockedUserDn");
        when(decodedJWT.getClaim("username")).thenReturn(claim("mockedUsername"));
    }
	 
	@Test
    void convert_DecodedJWTWithClaims_CreatesCustomLdapUserDetails() {
        CustomLdapUserDetails userDetails = jwtToPrincipalConverter.convert(decodedJWT);
        userDetails.eraseCredentials();
        assertEquals("mockedUserDn", userDetails.getDn());
        assertEquals("mockedUsername", userDetails.getUsername());
        assertNull(userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.isEnabled());
    }

    private static Claim claim(String value) {
        return mock(Claim.class, invocation -> value);
    }

}

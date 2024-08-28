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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtDecoderTest {
	@Mock
    private JwtProperties jwtProperties;
	
    @InjectMocks
    private JwtDecoder jwtDecoder;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtProperties.getSecretKey()).thenReturn("secretKey");
    }
    
    @Test
    void testDecodeValidToken() {
        String token = createValidToken("test");
        DecodedJWT result = jwtDecoder.decode(token);
        assertNotNull(result);
    }
    
    @Test
    void testDecodeInvalidToken() {
        Exception exception = assertThrows(JwtException.class, () -> jwtDecoder.decode("invalidToken"));
        assertEquals("Token invalid or expired", exception.getMessage());
    }

    
    private String createValidToken(String subject) {
        Algorithm algorithm = Algorithm.HMAC256(jwtProperties.getSecretKey());
        return JWT.create().withSubject(subject).sign(algorithm);
    }
}

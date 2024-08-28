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

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtDecoder {
	
	private final JwtProperties jwtProperties;
	
	public DecodedJWT decode(String token) {
		try {
			return JWT.require(Algorithm.HMAC256(jwtProperties.getSecretKey()))
					.build()
					.verify(token);		
		} catch (JWTVerificationException e) {
			log.error("Error is: {}", e.getMessage());
			throw new JwtException("Token invalid or expired", e);
		}

	}

}

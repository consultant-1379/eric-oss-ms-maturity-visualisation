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

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class JwtIssuer {
	
	private final JwtProperties jwtProperties;
	
	public String issue(String userDn, String username, Instant expiresAt) {
		return JWT.create()
				.withSubject(userDn)
				.withExpiresAt(expiresAt)
				.withClaim("username", username)
				.sign(Algorithm.HMAC256(jwtProperties.getSecretKey()));
	}

}

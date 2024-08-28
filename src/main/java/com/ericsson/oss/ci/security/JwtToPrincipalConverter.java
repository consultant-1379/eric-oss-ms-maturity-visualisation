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

import com.auth0.jwt.interfaces.DecodedJWT;


@Component
public class JwtToPrincipalConverter {
	
	public CustomLdapUserDetails convert(DecodedJWT jwt) {
		
		return CustomLdapUserDetails.builder()
				.userDn(jwt.getSubject())
				.username(jwt.getClaim("username").asString())
				.build();
	}
}

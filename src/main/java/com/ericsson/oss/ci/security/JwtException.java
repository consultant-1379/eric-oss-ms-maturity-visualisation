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

import org.springframework.security.core.AuthenticationException;

public class JwtException extends AuthenticationException {

	public JwtException(String msg) {
		super(msg);
	}
	
	public JwtException(String msg, Throwable cause) {
		super(msg, cause);
	}

}

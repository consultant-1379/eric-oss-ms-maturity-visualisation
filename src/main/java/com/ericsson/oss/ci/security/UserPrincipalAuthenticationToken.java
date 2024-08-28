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

import java.util.Objects;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class UserPrincipalAuthenticationToken extends AbstractAuthenticationToken {
	
	private static final long serialVersionUID = 2952003852354577542L;
	private final CustomLdapUserDetails userPrincipal;

	public UserPrincipalAuthenticationToken(CustomLdapUserDetails userPrincipal) {
		super(userPrincipal.getAuthorities());
		this.userPrincipal = userPrincipal;
	}
	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public CustomLdapUserDetails getPrincipal() {
		return userPrincipal;
	}

	@Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        UserPrincipalAuthenticationToken other = (UserPrincipalAuthenticationToken) obj;
        return Objects.equals(userPrincipal, other.userPrincipal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPrincipal);
    }

}

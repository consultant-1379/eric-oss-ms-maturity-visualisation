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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.jupiter.api.Test;

public class UserPrincipalAuthenticationTokenTest {

	@Test
    public void testUserPrincipalAuthenticationToken() {
        CustomLdapUserDetails userPrincipal1 = createUserPrincipal("username1");
        CustomLdapUserDetails userPrincipal2 = createUserPrincipal("username2");

        UserPrincipalAuthenticationToken token1 = new UserPrincipalAuthenticationToken(userPrincipal1);
        UserPrincipalAuthenticationToken token2 = new UserPrincipalAuthenticationToken(userPrincipal2);

        
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        assertEquals(userPrincipal1, token1.getPrincipal());
        assertEquals(Collections.emptyList(), token1.getAuthorities());
        assertNull(token1.getCredentials());
    }

    @Test
    public void testEqualsAndHashCode() {
        CustomLdapUserDetails userPrincipal1 = createUserPrincipal("username1");
        CustomLdapUserDetails userPrincipal2 = createUserPrincipal("username2");

        UserPrincipalAuthenticationToken token1 = new UserPrincipalAuthenticationToken(userPrincipal1);
        UserPrincipalAuthenticationToken token1Duplicate = new UserPrincipalAuthenticationToken(userPrincipal1);
        UserPrincipalAuthenticationToken token2 = new UserPrincipalAuthenticationToken(userPrincipal2);

        assertEquals(token1, token1Duplicate);
        assertNotEquals(null, token2);
        assertTrue(token2.equals(token2));
        assertNotEquals(token1, token2);
        assertEquals(token1.hashCode(), token1Duplicate.hashCode());
        assertNotEquals(token1.hashCode(), token2.hashCode());
    }

    private CustomLdapUserDetails createUserPrincipal(String username) {
        CustomLdapUserDetails userPrincipal = mock(CustomLdapUserDetails.class);
        return userPrincipal;
    }
}

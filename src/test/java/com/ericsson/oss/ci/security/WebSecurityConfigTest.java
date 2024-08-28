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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationProvider;

@SpringBootTest
public class WebSecurityConfigTest {
	
	    @Autowired
	    private WebSecurityConfig webSecurityConfig;

	    @Test
	    void configurationIsNotNull() {
	        assertThat(webSecurityConfig).isNotNull();
	    }
	    
	    @Test
	    void authenticationManagerBeanTest() throws Exception {
	        assertThat(webSecurityConfig.authenticationManagerBean()).isNotNull();
	    }

	    @Test
	    void activeDirectoryLdapAuthenticationProviderTest() {
	        AuthenticationProvider provider = webSecurityConfig.activeDirectoryLdapAuthenticationProvider("baseDn");
	        assertThat(provider).isNotNull();
	    }

}

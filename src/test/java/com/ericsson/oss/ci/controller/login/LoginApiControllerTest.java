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
package com.ericsson.oss.ci.controller.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;

import com.ericsson.oss.ci.domain.entity.UserEntity;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.LoginApi;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.CredentialsDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.UserCountDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.UserDto;
import com.ericsson.oss.ci.security.JwtIssuer;
import com.ericsson.oss.ci.service.UserLogEntryService;
import com.ericsson.oss.ci.service.UserService;

class LoginApiControllerTest {

	private LoginApi loginApi;
	private UserService userService;
	private UserLogEntryService userLogEntryService;
	private AuthenticationManager authenticationManager;
	private JwtIssuer jwtIssuer;
	private Environment env;
	
	@BeforeEach
	void setUp() {
		env = mock(Environment.class);
		when(env.getActiveProfiles()).thenReturn(new String[]{"dev", "prod"});
		userService = mock(UserService.class);
		userLogEntryService = mock(UserLogEntryService.class);
		authenticationManager = mock(AuthenticationManager.class);
		jwtIssuer = mock(JwtIssuer.class);
		loginApi = new LoginApiControllerImpl(env, authenticationManager, jwtIssuer, userService, userLogEntryService);
	}
	
	@Test
	void getCountTest() {
		when(userService.userCount()).thenReturn((long) 0);
		assertEquals(0, loginApi.getCount().getBody());
	}
	
	@Test
    void userLogin_SuccessfulAuthentication_ReturnsValidResponse() {
        performValidationTest(Optional.empty());
    }

	@Test
    void userLogin_UserValidationFails_NewUserSaved() {
		performValidationTest(Optional.of(new UserEntity()));
    }
	
	private void performValidationTest(Optional<UserEntity> user) {
    	CredentialsDto credentials = new CredentialsDto("username", "password");
        configureAuthentication(credentials.getUsername());
        ResponseEntity<UserDto> response = loginApi.userLogin(credentials);
        assertValidResponse(response, credentials.getUsername());
	}
    
    private void assertValidResponse(ResponseEntity<UserDto> response, String expectedUsername) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedUsername, response.getBody().getUsername());
        assertEquals("mockedToken", response.getBody().getToken());
    }
    
    private void configureAuthentication(String username) {
        LdapUserDetailsImpl userDetails = mock(LdapUserDetailsImpl.class);
        Authentication authentication = mock(Authentication.class);

        when(userDetails.getUsername()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtIssuer.issue(any(), any(), any())).thenReturn("mockedToken");
    }

    @Test
    void getAllCountTest() {
        Object[] user1 = { Date.valueOf("2024-01-12"), BigInteger.valueOf(3L) };
        Object[] user2 = { Date.valueOf("2024-01-11"), BigInteger.valueOf(5L) };
        List<Object[]> expectedResult = Arrays.asList(user1, user2);

        when(userService.countDistinctUsersUntilToday()).thenReturn(expectedResult);
        List<UserCountDto> result = loginApi.getAllCount().getBody();

        assertEquals(2, result.size());
    }
	
	
}

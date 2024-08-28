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


import java.math.BigInteger;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.oss.ci.domain.entity.UserEntity;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.LoginApi;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.CredentialsDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.UserCountDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.UserDto;
import com.ericsson.oss.ci.security.JwtIssuer;
import com.ericsson.oss.ci.service.UserLogEntryService;
import com.ericsson.oss.ci.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LoginApiControllerImpl implements LoginApi{
	
	 private final Environment env;
	 private final AuthenticationManager authenticationManager;
	 private final JwtIssuer jwtIssuer;
	 private final UserService userService;
	 private final UserLogEntryService userLogEntryService;

	@Override
    public ResponseEntity<UserDto> userLogin(@RequestBody @Validated CredentialsDto credentials) {
    	LdapUserDetailsImpl principal = authenticatedUserCredentials(credentials);
    	Instant expiresAt = Instant.now().plus(Duration.of(1, ChronoUnit.DAYS));
		String token = jwtIssuer.issue(principal.getDn(), principal.getUsername(), expiresAt);
		String userId = principal.getUsername();
		UserEntity userEntity = UserEntity.builder().userId(userId).build();

		String[] activeProfiles = env.getActiveProfiles();

        for (String profile : activeProfiles) {
            if ("prod".equals(profile)) {
                userService.saveUser(userEntity);
                userLogEntryService.storeUserLog(userId);
                break;
            }
        }

        return ResponseEntity.ok(
        		UserDto.builder()
        		.username(userId)
        		.token(token)
        		.expiresAt(expiresAt.toEpochMilli())
        		.build()
        	);
    }

	private LdapUserDetailsImpl authenticatedUserCredentials(CredentialsDto credentials) {
		Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                		credentials.getUsername(),
                		credentials.getPassword()
                )
            );
    	SecurityContextHolder.getContext().setAuthentication(authentication);
		return (LdapUserDetailsImpl) authentication.getPrincipal();
	}

	@Override
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(userService.userCount());
    }
	
	@Override
	public ResponseEntity<List<UserCountDto>> getAllCount(){
		List<Object[]> result = userService.countDistinctUsersUntilToday();
		
		List<UserCountDto> userCountList = result.stream()
		        .map(row -> new UserCountDto(
		            ((Date) row[0]).toLocalDate(),
		            ((BigInteger) row[1]).longValue()))
		        .collect(Collectors.toList());
 
		    return ResponseEntity.ok(userCountList);
	}
    
}

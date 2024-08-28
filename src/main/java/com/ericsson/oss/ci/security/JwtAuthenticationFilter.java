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

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private final JwtDecoder jwtDecoder;
	private final JwtToPrincipalConverter jwtToPrincipalConverter;
	private final CustomAuthEntryPoint customAuthEntryPoint;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		log.info(request.getRequestURI());
		try {
			
			extractTokenFromRequest(request)
			.map(jwtDecoder::decode)
			.map(jwtToPrincipalConverter::convert)
			.map(userPrincipal -> {
		        UserPrincipalAuthenticationToken authenticationToken = new UserPrincipalAuthenticationToken(userPrincipal);
		        authenticationToken.setAuthenticated(true);
		        return authenticationToken;
		    })
			.ifPresent(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication));	
			
		} catch (JwtException e) {
			SecurityContextHolder.clearContext();
			customAuthEntryPoint.commence(request, response, e);
		    return;
		}
		
		filterChain.doFilter(request, response);
	}
	
	
	private Optional<String> extractTokenFromRequest(HttpServletRequest request){
		return  Optional.ofNullable(request.getHeader("Authorization"))
				.filter(token -> StringUtils.hasText(token) && token.startsWith("Bearer "))
				.map(t -> t.substring(7));
	}

}

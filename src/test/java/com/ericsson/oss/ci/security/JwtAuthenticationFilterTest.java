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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtAuthenticationFilterTest {

	@Mock
    private JwtDecoder jwtDecoder;
	
	@Mock
	private FilterChain filterChain;

    @Mock
    private JwtToPrincipalConverter jwtToPrincipalConverter;

    @Mock
    private CustomAuthEntryPoint customAuthEntryPoint;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void doFilterInternal_ValidToken_SuccessfulAuthentication() throws ServletException, IOException {
    	String validToken = "validToken";
        DecodedJWT decodedJWT = createMockDecodedJWT();
        when(jwtDecoder.decode(validToken)).thenReturn(decodedJWT);
        CustomLdapUserDetails expectedPrincipal = createMockPrincipal();
        when(jwtToPrincipalConverter.convert(decodedJWT)).thenReturn(expectedPrincipal);

        jwtAuthenticationFilter.doFilterInternal(createRequestWithToken(validToken), new MockHttpServletResponse(), filterChain);

        verifyFilterChainInvocation();
        verify(customAuthEntryPoint, never()).commence(any(), any(), any());
    }
    
    @Test
    void doFilterInternal_InvalidToken_CustomAuthEntryPointCalled() throws ServletException, IOException {
        String invalidToken = "invalidToken";

        when(jwtDecoder.decode(invalidToken)).thenThrow(new JwtException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(createRequestWithToken(invalidToken), new MockHttpServletResponse(), filterChain);

        verify(customAuthEntryPoint).commence(any(), any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }



    private DecodedJWT createMockDecodedJWT() {
        return mock(DecodedJWT.class);
    }
    
    private CustomLdapUserDetails createMockPrincipal() {
        CustomLdapUserDetails mockPrincipal = mock(CustomLdapUserDetails.class);
        when(mockPrincipal.getUserDn()).thenReturn("mockedUserDn");
        when(mockPrincipal.getUsername()).thenReturn("mockedUsername");
        return mockPrincipal;
    }
    
    private MockHttpServletRequest createRequestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private void verifyFilterChainInvocation() throws IOException, ServletException {
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        ArgumentCaptor<ServletResponse> responseCaptor = ArgumentCaptor.forClass(ServletResponse.class);
        verify(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

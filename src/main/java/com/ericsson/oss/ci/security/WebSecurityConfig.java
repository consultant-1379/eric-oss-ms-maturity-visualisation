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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter  {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final LdapProperties ldapProperties;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		
		http
		.csrf(csrf -> csrf.disable())
		.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
	    .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	    .formLogin().disable()
	    .authorizeRequests()
            .antMatchers("/actuator/**", "/api/v1/count","/api/v1/all-count", "/").permitAll()
	    	.antMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
	    	.antMatchers("/api/v1/**").authenticated()
	    .and()
	    .exceptionHandling()
	        .authenticationEntryPoint(customAuthEntryPoint);
    }
	
 
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
    		auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider1());
    		auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider2());
    		auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider3());
    }
    
    @Bean
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider1() {
    	return activeDirectoryLdapAuthenticationProvider(ldapProperties.getBaseDn1());
    }
    
    @Bean
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider2() {
    	return activeDirectoryLdapAuthenticationProvider(ldapProperties.getBaseDn2());
    }
    
    @Bean
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider3() {
    	return activeDirectoryLdapAuthenticationProvider(ldapProperties.getBaseDn3());
    }
      
    
	@Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
	
    
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider(String baseDn) {
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(ldapProperties.getDomain(), ldapProperties.getUrl(), baseDn);
        provider.setSearchFilter(ldapProperties.getSearchFilter());
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        return provider;
    }
    
    
}

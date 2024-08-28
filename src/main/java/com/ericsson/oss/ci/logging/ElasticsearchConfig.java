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
package com.ericsson.oss.ci.logging;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
	
	private final ElasticsearchProperties properties;
	
	@Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));
        
		 RestClientBuilder builder = RestClient.builder(
	        		new HttpHost(properties.getUrl(), 443, "https"))
	                .setHttpClientConfigCallback(
	                   httpClientBuilder -> httpClientBuilder
			                 .setDefaultCredentialsProvider(credentialsProvider)
			                 .setSSLContext(sslContext())
			                 .setDefaultHeaders(compatibilityHeaders())
			                 .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
			          );

	        return new RestHighLevelClient(builder);
	}
	
	public SSLContext sslContext() {
        try {
            return SSLContextBuilder.create()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new SSLContextConfigurationException("Error while configuring SSL context", e);
        }
    }
	
	private Collection<Header> compatibilityHeaders() {
	    return Collections.unmodifiableList(
	        Arrays.asList(
	            new BasicHeader(HttpHeaders.ACCEPT, properties.getCompatibleHeader()),
	            new BasicHeader(HttpHeaders.CONTENT_TYPE, properties.getCompatibleHeader())
	        )
	    );
	}
}


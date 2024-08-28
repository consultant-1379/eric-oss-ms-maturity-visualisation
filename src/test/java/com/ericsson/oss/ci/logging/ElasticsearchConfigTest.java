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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ElasticsearchConfigTest {

	@Mock
    private ElasticsearchProperties properties;
	
	@InjectMocks
    private ElasticsearchConfig config;

    @Test
    void shouldCreateRestHighLevelClientWithValidCredentials() {
    	when(properties.getUsername()).thenReturn("user");
        when(properties.getPassword()).thenReturn("password");
        when(properties.getUrl()).thenReturn("https://localhost:9200");

        assertThat(config.elasticsearchClient()).isNotNull();
    }

}

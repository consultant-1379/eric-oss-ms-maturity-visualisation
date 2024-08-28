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
package com.ericsson.oss.ci.service.impl;


import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import com.ericsson.oss.ci.logging.RestHighLevelClientWrapper;
import com.ericsson.oss.ci.logging.UserLogEntry;
import com.ericsson.oss.ci.service.UserLogEntryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogEntryServiceImpl implements UserLogEntryService  {
	
	private final RestHighLevelClientWrapper restHighLevelClientWrapper;
	private final ObjectMapper objectMapper;

	@Override
	@PostConstruct
	public void setUp() {
		this.objectMapper.registerModule(new JavaTimeModule());
	}
	
	
	@Override
	public void storeUserLog(String username) {
	    try {
	        Optional<String> userJsonString = Optional.ofNullable(objectMapper.writeValueAsString(createLogEntry(username)));

	        userJsonString.ifPresent(json -> {
	            String indexName = "mrv-user-logs";
	            IndexRequest request = new IndexRequest(indexName).source(json, XContentType.JSON);
	            
	            try {
	                IndexResponse indexResponse = restHighLevelClientWrapper.index(request, RequestOptions.DEFAULT);
	                Optional.ofNullable(indexResponse)
	                        .ifPresent(response -> 
	                        log.info("User logged info stored to elastic search successfully into index: {}", response.getIndex())
	                        );
	            } catch (IOException e) {
	                log.error("Failed to store user logged info to elastic search for user: " + username, e);
	            }
	        });
	    } catch (JsonProcessingException e) {
	        log.error("Failed to convert user log entry to JSON for user: " + username, e);
	    } 
	}

	
	public UserLogEntry createLogEntry(String username) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String timestampString = Instant.now().atZone(ZoneOffset.UTC).format(formatter);

		return UserLogEntry.builder()
                .timestamp(timestampString)
                .username(username)
                .message(UserLogEntry.Message.builder().info("Login success").build())
                .severity("info")
                .service(UserLogEntryService.class.getName())
                .action("login")
                .actionParameters(UserLogEntry.ActionParameters.builder()
                        .sortField("createdOn")
                        .sortOrder(false)
                        .filter(UserLogEntry.ActionParameters.Filter.builder()
                                .start(timestampString)
                                .build())
                        .build())
                .source("MRV")
                .build();
	}


}

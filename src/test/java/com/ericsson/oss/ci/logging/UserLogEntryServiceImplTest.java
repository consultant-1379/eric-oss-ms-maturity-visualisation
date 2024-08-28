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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.ci.service.UserLogEntryService;
import com.ericsson.oss.ci.service.impl.UserLogEntryServiceImpl;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

public class UserLogEntryServiceImplTest {

    private UserLogEntryService userLogEntryService;
    private RestHighLevelClientWrapper clientWrapper;
    private ObjectMapper objectMapper;
    private final String test_username = getRandomString();
    
    
    private Appender<ILoggingEvent> mockAppender;
    private ArgumentCaptor<ILoggingEvent> logCaptor;
    
    @BeforeEach
    void init() {
    	clientWrapper = mock(RestHighLevelClientWrapper.class);
    	objectMapper = mock(ObjectMapper.class);
    	userLogEntryService = new UserLogEntryServiceImpl(clientWrapper, objectMapper);
    	
    	Logger logger = (Logger) LoggerFactory.getLogger(UserLogEntryServiceImpl.class);
        mockAppender = mock(Appender.class);
        logger.addAppender(mockAppender);
        logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
    }
    
    @Test
    public void testSetUp() {
    	userLogEntryService.setUp();
        verify(objectMapper).registerModule(any(JavaTimeModule.class));
    }
    
    @Test
    public void testStoreUserLog() throws IOException {
        doReturn( mock(IndexResponse.class)).when(clientWrapper).index(any(IndexRequest.class), any(RequestOptions.class));
        when(objectMapper.writeValueAsString(any(UserLogEntry.class))).thenReturn("Hello");

        userLogEntryService.storeUserLog(test_username);
        verify(clientWrapper, times(1)).index(any(IndexRequest.class), any(RequestOptions.class));
    }
    
    @Test
    public void testStoreUserLog_JsonProcessingException() throws IOException {
    	 doThrow(new JsonMappingException(null, "")).when(objectMapper).writeValueAsString(any(UserLogEntry.class));
    	 userLogEntryService.storeUserLog(test_username);

         verify(mockAppender, times(1)).doAppend(logCaptor.capture());
         ILoggingEvent loggingEvent = logCaptor.getValue();
         assertTrue(loggingEvent.getFormattedMessage().contains("Failed to convert user log entry to JSON for user: " + test_username));
         assertEquals(Level.ERROR, loggingEvent.getLevel());
    }
    
    @Test
    public void testStoreUserLog_IOException() throws IOException {
        when(objectMapper.writeValueAsString(any(UserLogEntry.class))).thenReturn("Hello");
        doThrow(new IOException()).when(clientWrapper).index(any(IndexRequest.class), any(RequestOptions.class));

        userLogEntryService.storeUserLog(test_username);

        verify(mockAppender, times(1)).doAppend(logCaptor.capture());
        ILoggingEvent loggingEvent = logCaptor.getValue();
        assertTrue(loggingEvent.getFormattedMessage().contains("Failed to store user logged info to elastic search for user: " + test_username));
        assertEquals(Level.ERROR, loggingEvent.getLevel());
    }


    private final static String getRandomString() {
        return UUID.randomUUID().toString();
    }
}

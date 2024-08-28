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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLogEntry {

    private String username; 
    private String severity;
    private String service;
    private String action;
    private String source;

    @JsonProperty("@timestamp")
    private String timestamp;
    
    @JsonProperty("message")
    private Message message;
    
    @JsonProperty("actionParameters")
    private ActionParameters actionParameters;
    
    @Data
    @Builder
    public static class Message {
    	private final String info;
    }

    @Data
    @Builder
    public static class ActionParameters {
        private final String sortField;
        private final boolean sortOrder;
        private final Filter filter;

        @Data
        @Builder
        public static class Filter {
            private final String start;
        }
    }
}

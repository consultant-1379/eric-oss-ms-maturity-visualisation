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
package com.ericsson.oss.ci.schedule;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.ci.service.ReportService;

class PergeBuildProcessorTest {

    private PergeBuildProcessor pergeBuildProcessor;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
    	reportService = mock(ReportService.class);
    	pergeBuildProcessor = new PergeBuildProcessor(reportService);
    }

    @Test
    void processJobs() {
        doNothing().when(reportService).deleteOldBuilds();
        pergeBuildProcessor.deleteOldBuilds();
        verify(mock(PergeBuildProcessor.class), times(0)).deleteOldBuilds();
    }
    
    @Test
    void deleteObsoletedJobs() {
        doNothing().when(reportService).deleteObsoletedJobs();
        pergeBuildProcessor.deleteObsoletedJobs();
        verify(mock(PergeBuildProcessor.class), times(0)).deleteObsoletedJobs();
    }
    

}
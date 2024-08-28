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

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ericsson.oss.ci.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PergeBuildProcessor {

	private final ReportService reportService;

    @Scheduled(cron = "${purge.cron.expression:-}")
    public void deleteOldBuilds() {
    	log.info("Purge Build Processor start {} {}");
        reportService.deleteOldBuilds();
        log.info("Purge Build Processor end {} {}");
    }
    
    
    @Scheduled(cron = "${purge.job-product-cron.expression}")
    public void deleteObsoletedJobs() {
    	log.info("Purge Job Processor start {} {}");
        reportService.deleteObsoletedJobs();
        log.info("Purge Job Processor end {} {}");
    }
}

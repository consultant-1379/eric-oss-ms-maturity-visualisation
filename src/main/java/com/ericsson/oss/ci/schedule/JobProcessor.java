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
import com.ericsson.oss.ci.service.JenkinsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobProcessor {

    private final JenkinsService jenkinsService;

    @Scheduled(cron = "${processor.cron.expression}")
    public void processJobs() {
        log.info("Job process start process jobs");
        jenkinsService.getJobs().forEach(jenkinsService::processJob);
        log.info("Job process end process jobs");
        
    }
}

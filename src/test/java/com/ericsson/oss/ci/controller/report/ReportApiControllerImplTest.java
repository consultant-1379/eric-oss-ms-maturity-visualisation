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
package com.ericsson.oss.ci.controller.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.oss.ci.ms.maturity.visualisation.api.ReportApi;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ProductResponseDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportListDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportsDto;
import com.ericsson.oss.ci.service.ReportService;

class ReportApiControllerImplTest {
    private ReportApi reportApi;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        reportApi = new ReportApiControllerImpl(reportService);
    }

    @Test
    void getReportTest() {
        final ReportDto reports = new ReportDto();
        ReportsDto reportsDto = new ReportsDto();
        reportsDto.setReports(reports);
        when(reportService.getProductReports(any(),any(),any(),any(),any())).thenReturn(reportsDto);
        assertEquals(reportsDto, reportApi.getReport("product","appType","type",0,1).getBody());
    }
    
    @Test
    void getReportsTest() {
            final List<ReportDto> reports = List.of(new ReportDto(), new ReportDto());
            ReportListDto reportsDto = new ReportListDto();
            reportsDto.setReports(reports);
            when(reportService.getProductReport(any(),any(),any())).thenReturn(reportsDto);
            assertEquals(reportsDto, reportApi.getReports("product",0,1).getBody());
        }

    @Test
    void getProductsTest() {
    	final ProductResponseDto productResponseDto=new ProductResponseDto();
        final Set<String> jobNotification=Set.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        productResponseDto.setJobNotification(jobNotification);
        when(reportService.getProducts()).thenReturn(productResponseDto);
        assertEquals(productResponseDto, reportApi.getProducts().getBody());
    }
}
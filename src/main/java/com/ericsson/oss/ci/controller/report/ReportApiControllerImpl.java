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


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.oss.ci.ms.maturity.visualisation.api.ReportApi;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ProductResponseDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportListDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportsDto;
import com.ericsson.oss.ci.service.ReportService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ReportApiControllerImpl implements ReportApi {

	private final ReportService reportService;

    @Override
    public ResponseEntity<ReportsDto> getReport(final String product,final String appType,final String type, final Integer pageNo, final Integer pageSize) {
        return ResponseEntity.ok(reportService.getProductReports(product,appType,type, pageNo, pageSize));
    }

    @Override
    public ResponseEntity<ProductResponseDto> getProducts() {
        return ResponseEntity.ok(reportService.getProducts());
    }	

    @Override
	public ResponseEntity<ReportListDto> getReports(final String product,  final Integer pageNo,
	final Integer pageSize) {
		return ResponseEntity.ok(reportService.getProductReport(product, pageNo, pageSize));
	}

}

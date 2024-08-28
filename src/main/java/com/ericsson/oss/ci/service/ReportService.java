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
package com.ericsson.oss.ci.service;



import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ProductResponseDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportListDto;
import com.ericsson.oss.ci.ms.maturity.visualisation.api.model.ReportsDto;

public interface ReportService {
	ProductResponseDto getProducts();
    
    ReportsDto getProductReports(String product,String appType,String type, Integer pageNo, Integer pageSize);

    ReportListDto getProductReport(String product,Integer pageNo, Integer pageSize);

	void deleteOldBuilds();
	
	void deleteObsoletedJobs();
}

package com.log.analysis.report.controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.log.analysis.elasticsearch.model.ReportData;
import com.log.analysis.report.service.PdfReportGenerator;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {
	
	@Autowired
	public PdfReportGenerator  reportGenerator;
	
	
	
	@GetMapping("/generate-report")
    public String generateReport(@RequestBody Map<String, Object> logData) {
        // Retrieve the data and create the ReportData object
        ReportData reportData = reportGenerator.retrieveData(logData);

        // Generate the PDF report
        reportGenerator.generateReport(reportData, "N:/PFE/Back/report.pdf");

        return "PDF report generated successfully.";
    }
	

}

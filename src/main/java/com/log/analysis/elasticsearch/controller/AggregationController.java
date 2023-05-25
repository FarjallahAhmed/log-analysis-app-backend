package com.log.analysis.elasticsearch.controller;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log.analysis.elasticsearch.AggregationLogsService;



@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/agg")
public class AggregationController {
	
	@Autowired
	private AggregationLogsService aggs;
	
	
	@GetMapping(path = "/logs-per-month/{index}")
	public ResponseEntity<Map<String, Long>> getLogsPerMonth(@PathVariable("index") String index){
		try {
			Map<String, Long> results = aggs.getLogsPerMonth(index);
			return new ResponseEntity<>(results,HttpStatus.OK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/logs-top-message/{index}")
	public ResponseEntity<Map<String, Long>> getTopMessage(@PathVariable("index") String index){
		try {
			Map<String, Long> results = aggs.getTopMessage(index);
			return new ResponseEntity<Map<String,Long>>(results,HttpStatus.OK);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/logs-date-range/{index}")
	public ResponseEntity<List<String>> getLogsDateRange(@PathVariable("index") String index){
		try {
			List<String> results = aggs.getDateRangeOfLogs(index);
			return new ResponseEntity<List<String>>(results,HttpStatus.OK);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/summary/{index}")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable("index") String index) {
        try {
            Map<String, Object> summary = aggs.generateSummary(index);
            return new ResponseEntity<>(summary, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
	@PostMapping("/report")
    public ResponseEntity<String> generateReport(@RequestBody Map<String, Object> logData) throws IOException {
		
        try {
			aggs.generateReportPDF(logData,"report.pdf");
			
		} catch (com.itextpdf.text.DocumentException e) {
			
			e.printStackTrace();
		}
        return ResponseEntity.ok("Report generated successfully");
    }
	
}

package com.log.analysis.elasticsearch.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log.analysis.elasticsearch.AggregationLogsService;
import com.lowagie.text.DocumentException;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/agg")
public class AggregationController {
	
	@Autowired
	private AggregationLogsService aggs;
	
	
	@GetMapping(path = "/logs-per-month")
	public ResponseEntity<Map<String, Long>> getLogsPerMonth(){
		try {
			Map<String, Long> results = aggs.getLogsPerMonth();
			return new ResponseEntity<>(results,HttpStatus.OK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/logs-top-message")
	public ResponseEntity<Map<String, Long>> getTopMessage(){
		try {
			Map<String, Long> results = aggs.getTopMessage();
			return new ResponseEntity<Map<String,Long>>(results,HttpStatus.OK);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/logs-date-range")
	public ResponseEntity<List<String>> getLogsDateRange(){
		try {
			List<String> results = aggs.getDateRangeOfLogs();
			return new ResponseEntity<List<String>>(results,HttpStatus.OK);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        try {
            Map<String, Object> summary = aggs.generateSummary();
            return new ResponseEntity<>(summary, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
	@PostMapping("/report")
    public ResponseEntity<String> generateReport(@RequestBody Map<String, Object> logData) {
        aggs.generateReport(logData);
        return ResponseEntity.ok("Report generated successfully");
    }
}

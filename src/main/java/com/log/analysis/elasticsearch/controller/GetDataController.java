package com.log.analysis.elasticsearch.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.log.analysis.elasticsearch.FilterLogsService;
import com.log.analysis.elasticsearch.GetDataService;
import com.log.analysis.elasticsearch.model.Default;
import com.log.analysis.elasticsearch.model.ExceptionDefault;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/logs")
public class GetDataController {
	
	@Autowired
	private GetDataService getDataService;
	
	@Autowired
	private FilterLogsService filterLogsService;
	
	
	@GetMapping(path = "simplelogs")
	public ResponseEntity<Page<Default>> getDefaultSimpleLogs(Pageable pageable){
		try {
			Page<Default> myDataList = getDataService.getSimpleLogs(pageable);
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(path = "exceptionlogs")
	public ResponseEntity<Page<ExceptionDefault>> getDefaultExceptionogs(Pageable pageable){
		try {
			Page<ExceptionDefault> myDataList = getDataService.getExceptionLogs(pageable);
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@GetMapping("filter/errormessage")
	public ResponseEntity<List<ExceptionDefault>> filterErrorException(@RequestParam("errorMessage") String errorMessage){
	
		try {
			List<ExceptionDefault> myDataList = getDataService.getLogsWithSpecificMessage(errorMessage);
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("filter/loglevel")
	public ResponseEntity<List<Default>> filterLogLevel(@RequestParam("loglevel") String loglevel){
	
		try {
			List<Default> myDataList = filterLogsService.filterDefaultWithLogLevel(loglevel);
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@GetMapping("filter/logmessage")
	public ResponseEntity<List<Default>> filterLogByMessage(@RequestParam("logmessage") String logmessage){
	
		try {
			List<Default> myDataList = filterLogsService.filterLogsByMessageKeyword(logmessage);
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@GetMapping("filter/logs-by-date-range")
	public ResponseEntity<Map<String, Object>> getLogsByDateRange(
	        @RequestParam(name = "start-date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
	        @RequestParam(name = "end-date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) throws IOException {
		
		Map<String, Object> response = filterLogsService.getLogsByDateRange(startDate, endDate);
		
		return new ResponseEntity<>(response,HttpStatus.OK);
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

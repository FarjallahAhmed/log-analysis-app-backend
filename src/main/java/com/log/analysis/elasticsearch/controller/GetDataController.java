package com.log.analysis.elasticsearch.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log.analysis.elasticsearch.GetDataService;
import com.log.analysis.elasticsearch.model.Default;
import com.log.analysis.elasticsearch.model.ExceptionDefault;


@RestController
@RequestMapping("/api/logs")
public class GetDataController {
	
	@Autowired
	private GetDataService getDataService;
	
	@GetMapping(path = "simplelogs")
	public ResponseEntity<List<Default>> getDefaultSimpleLogs(){
		try {
			List<Default> myDataList = getDataService.getSimpleLogs();
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping(path = "exceptionlogs")
	public ResponseEntity<List<ExceptionDefault>> getDefaultExceptionogs(){
		try {
			List<ExceptionDefault> myDataList = getDataService.getExceptionLogs();
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

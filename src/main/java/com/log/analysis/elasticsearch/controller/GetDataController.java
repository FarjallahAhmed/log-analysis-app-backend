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


@RestController
@RequestMapping("/api/logs")
public class GetDataController {
	
	@Autowired
	private GetDataService getDataService;
	
	@GetMapping
	public ResponseEntity<List<Default>> getDefaultData(){
		try {
			List<Default> myDataList = getDataService.getData();
			return new ResponseEntity<> (myDataList,HttpStatus.OK);
		}catch (IOException e) {
			e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}

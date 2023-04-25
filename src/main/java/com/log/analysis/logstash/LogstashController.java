package com.log.analysis.logstash;


import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class LogstashController {
	
	@Autowired
	private LoadDataService lds;
	
	
	@PostMapping("/load-data")
	public void loadDataFromFile(@RequestParam("pathFile") String pathFile,
			@RequestParam("pattern") String pattern,
			@RequestParam("logstashFile") String logstashFile) {
		
		lds.loadDataFromPathFile(pathFile,pattern,logstashFile);
		
	}




}

package com.log.analysis.logstash;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@CrossOrigin(origins = "http://localhost:4200")
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

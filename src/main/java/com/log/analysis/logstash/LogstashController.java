package com.log.analysis.logstash;



import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

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
	
	
	@PostMapping("/load-data-from-file")
	public void loadDataFromFile(@RequestParam("pathFile") String pathFile,
			@RequestParam("pattern") String pattern,
			@RequestParam("logstashFile") String logstashFile) {
		
		lds.loadDataAndStartLogstash(pathFile, pattern, logstashFile);
	}
	
	


	
	@PostMapping("/load-data-from-directory")
	public void startLogstash(@RequestParam("directory") String directory,@RequestParam("logstashFile") String logstashFile) {
		String pathLogConfig = "C:/Elastic stack/logstash/"+logstashFile;
		try {
		    
		    String logstashConfig = logstashFile;
		    String modifiedConfig = lds.modifyLogstashConfig(pathLogConfig, directory);

		    ProcessBuilder processBuilder = new ProcessBuilder("C:/Elastic stack/logstash/bin/logstash.bat", "-f", modifiedConfig);
		    processBuilder.redirectErrorStream(true); 
		    Process process = processBuilder.start();

		    InputStream inputStream = process.getInputStream();
		    // Read the process output
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		    String line;
		    boolean reading = true; 

		    while (reading) {
		        line = reader.readLine();
		        if (line == null) {
		            reading = false; 
		        } else {
		            System.out.println(line);
		        }
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    
		}

	}




}

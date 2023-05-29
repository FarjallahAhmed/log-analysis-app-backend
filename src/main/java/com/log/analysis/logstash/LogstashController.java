package com.log.analysis.logstash;



import java.io.BufferedReader;
import java.io.IOException;
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
	
	
	@PostMapping("/load-data")
	public void loadDataFromFile(@RequestParam("pathFile") String pathFile,
			@RequestParam("pattern") String pattern,
			@RequestParam("logstashFile") String logstashFile) {
		
		lds.loadDataFromPathFile(pathFile,pattern,logstashFile);
		
	}
	
	
	@PostMapping("/start-logstash")
	public void startLogstash() {
	  try {
	    ProcessBuilder processBuilder = new ProcessBuilder("C:/Elastic stack/logstash/bin/logstash.bat");/*, "-f", "C:/Elastic stack/logstash/testlog.conf")*/;
	    processBuilder.redirectErrorStream(true); // Redirect the error stream to the output stream
	    Process process = processBuilder.start();

	    // Read the process output
	    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    String line;

	    while ((line = reader.readLine()) != null) {
	      System.out.println(line);
	    }

	    int exitCode = process.waitFor(); // Wait for the process to finish
	    if (exitCode == 0) {
	      System.out.println("Logstash process exited successfully.");
	    } else {
	      System.out.println("Logstash process exited with an error: " + exitCode);
	    }

	  } catch (Exception e) {
	    e.printStackTrace();
	    // Handle the exception as needed
	  }
	}
	
	@PostMapping("/start-logstash-directory")
	public void startLogstash(@RequestParam("directory") String directory) {
		
		try {
		    // Modify the Logstash configuration file dynamically
		    String logstashConfig = "C:/Elastic stack/logstash/testlog.conf";
		    String modifiedConfig = lds.modifyLogstashConfig(logstashConfig, directory);

		    ProcessBuilder processBuilder = new ProcessBuilder("C:/Elastic stack/logstash/bin/logstash.bat", "-f", modifiedConfig);
		    processBuilder.redirectErrorStream(true); // Redirect the error stream to the output stream
		    Process process = processBuilder.start();

		    // Read the process output
		    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    String line;
		    

		    while ((line = reader.readLine()) != null ) {
		    	 System.out.println(line);
		    }

		    int exitCode = process.waitFor(); // Wait for the process to finish
		    if (exitCode == 0) {
		      System.out.println("Logstash process exited successfully.");
		    } else {
		      System.out.println("Logstash process exited with an error: " + exitCode);
		    }

		  } catch (Exception e) {
		    e.printStackTrace();
		    // Handle the exception as needed
		  }
	}




}

package com.log.analysis.logstash;


import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class LogstashController {
	


	  @PostMapping("/set-env")
	  public String setEnv(@RequestBody String envVariable) {
	    String[] parts = envVariable.split("=");
	    if (parts.length != 2) {
	        return "Invalid input format. Please provide the input as 'key=value'.";
	    }
	    String key = parts[0];
	    String value = parts[1];
	    System.setProperty(key, value);
	    return "Environment variable " + key + " set to " + value;
	  }
	  
	  
	@PostMapping("/logstash")
	public ResponseEntity<String> runLogstash(@RequestBody LogstashRequest request){
		try {
			String relativePath = "C:\\Elastic stack\\logs\\Default.log"; // relative path to your input file
		    Path absolutePath = Paths.get("").toAbsolutePath().resolve(relativePath);
		    String inputPath = absolutePath.toString().replace("\\", "/");
		    System.setProperty("INPUT_PATH", inputPath);
			System.out.println("INPUT_PATH: " + System.getProperty("INPUT_PATH"));
			//Runtime.getRuntime().exec("C:\\Elastic stack\\logstash\\bin\\logstash.bat -f testlog.conf");
		
			return ResponseEntity.ok("Logstash command excuted successfully+ ARG1="+ request.getArg1()+" -E ARG2="+request.getArg2());
			
		}catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to execute Logstash command");
		}
	}
	
	@GetMapping("/setenv")
	public String setEnv() {
	    String relativePath = "C:\\Elastic stack\\logs\\Default.log"; // relative path to your input file
	    Path absolutePath = Paths.get("").toAbsolutePath().resolve(relativePath);
	    String inputPath = absolutePath.toString().replace("\\", "/");
	    System.setProperty("INPUT_PATH", inputPath);
	    System.out.println("INPUT_PATH: " + System.getProperty("INPUT_PATH")); // print the value of INPUT_PATH
	    return "Environment variable set.";
	}




}

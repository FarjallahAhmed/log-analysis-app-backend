package com.log.analysis.logstash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;


@Service
public class LoadDataService {

	public void loadDataFromPathFile(String pathFile,String pattern,String logstashFile) {
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(logstashFile));
			StringBuilder sb = new StringBuilder();
			String line ;
			while((line = reader.readLine()) != null ) {
				
				if(line.trim().startsWith("path =>")) {
					sb.append("    path => \"").append(pathFile).append("\"\n");
					
				}else if(line.trim().startsWith("match => { \"message\" =>")) {
					
					sb.append("    match => { \"message\" => \"").append(pattern).append("\"}\n");
				}
				else {
					sb.append(line).append("\n");
				}
				
			}
			reader.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(pathFile));
			writer.write(sb.toString());
			writer.close();
		}catch (IOException e){ 
			System.out.println("An error occurred.");
            e.printStackTrace();
		}
		
	}
	
	
	
	
	
	public String modifyLogstashConfig(String configFilePath, String directory) throws IOException {
		
		  String config = Files.readString(Paths.get(configFilePath));
		  String modifiedConfig = config.replace("/path/to/directory/*", directory);
		  
		  // Write the modified configuration to a temporary file
		  Path tempFile = Files.createTempFile("logstash", ".conf");
		  Files.writeString(tempFile, modifiedConfig);

		  return tempFile.toAbsolutePath().toString();
	}
	
	
	
	
	
	
	
}

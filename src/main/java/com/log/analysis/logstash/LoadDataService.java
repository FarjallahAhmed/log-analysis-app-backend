package com.log.analysis.logstash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;


@Service
public class LoadDataService {

	public void loadDataFromPathFile(String pathFile,String pattern,String logstashFile) {
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(logstashFile));
			System.out.println("open Logstash file: "+logstashFile);
			StringBuilder sb = new StringBuilder();
			String line ;
			while((line = reader.readLine()) != null ) {
				
				if(line.trim().startsWith("path =>")) {
					sb.append("    path => \"").append(pathFile).append("\"\n");
					
				}else if(line.trim().startsWith("match => { \"message\" =>") && pattern != "") {
					
					sb.append("    match => { \"message\" => \"").append(pattern).append("\"}\n");
				}
				else {
					sb.append(line).append("\n");
				}
				
			}
			reader.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(logstashFile));
			writer.write(sb.toString());

			writer.close();
		}catch (IOException e){ 
			System.out.println("An error occurred.");
            e.printStackTrace();
		}
		
	}
	

	public void startLogstash(String logstashFile) {
		try {
		    
		    ProcessBuilder processBuilder = new ProcessBuilder("C:/Elastic stack/logstash/bin/logstash.bat", "-f", logstashFile);
		    processBuilder.redirectErrorStream(true); // Redirect the error stream to the output stream
		    Process process = processBuilder.start();

		    InputStream inputStream = process.getInputStream();
		    // Read the process output
		    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		    String line;
		    boolean reading = true; // Flag to track reading status

		    while (reading) {
		        line = reader.readLine();
		        if (line == null) {
		            reading = false; // Exit the loop if line is null, empty, or contains only whitespace
		        } else {
		            System.out.println(line);
		        }
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    // Handle the exception as needed
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
	
	
	public void loadDataAndStartLogstash(String pathFile, String pattern, String logstashFile) {
		String pathLogConfig = "C:/Elastic stack/logstash/"+logstashFile;
		String PathLog = "C:/Elastic stack/logs/"+pathFile;
        try {
            // Step 1: Load data from path file into Logstash configuration
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(pathLogConfig));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("path =>") ) {
                    sb.append("    path => \"").append(PathLog).append("\"\n");
                } /*else if (line.trim().startsWith("match => { \"message\" =>") && pattern != "empty") {
                    sb.append("    match => { \"message\" => \"").append(pattern).append("\"}\n");
                }*/ else {
                    sb.append(line).append("\n");
                }
            }
            reader.close();

            // Write the modified content back to the Logstash configuration file
            BufferedWriter writer = new BufferedWriter(new FileWriter(pathLogConfig));
            writer.write(sb.toString());
            writer.close();

            // Step 2: Start Logstash
            ProcessBuilder processBuilder = new ProcessBuilder("C:/Elastic stack/logstash/bin/logstash.bat", "-f", pathLogConfig);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read and print the Logstash process output
            InputStream inputStream = process.getInputStream();
            BufferedReader processReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = processReader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor(); // Wait for the Logstash process to complete
            processReader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Handle the exceptions as needed
        }
    }
	
	
	
	
	
	
	
	
}

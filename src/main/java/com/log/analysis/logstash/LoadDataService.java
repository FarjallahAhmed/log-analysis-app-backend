package com.log.analysis.logstash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
}

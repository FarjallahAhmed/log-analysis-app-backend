package com.log.analysis.elasticsearch.model;




import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Default {
	
	private String loglevel;
	private String logger;
	private String errorMessage;
	private String stackTrace;
	private String day;
	private String month;
	private String year;
	private String logDate;
	private String logMessage;
	private String packagee;
	private String threadName;
	
}

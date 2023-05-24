package com.log.analysis.alerting.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.log.analysis.alerting.repository.AlertConfigurationRepository;
import com.log.analysis.elasticsearch.model.AlertConfiguration;
import com.log.analysis.elasticsearch.model.Default;
import com.log.analysis.elasticsearch.model.ExceptionDefault;

@Service
public class AlertConfigurationService {
	
	@Autowired
	private  AlertConfigurationRepository alertConfigurationRepository;
    

   
	
	public List<Default> getRelevantLogs(AlertConfiguration alertConfiguration, List<Default> logDataList) {
	    List<Default> relevantLogs = new ArrayList<>();

	    // Implement logic to filter log data based on the trigger condition of the alert configuration
	    // For example, filter log data by log level or log pattern

	    // Example logic for demonstration purposes:
	    for (Default logData : logDataList) {
	        if (alertConfiguration.getTriggerCondition().equals("logLevel")) {
	            // Filter log data by log level
	            if (logData.getLoglevel().equals("ERROR")) {
	                relevantLogs.add(logData);
	            }
	        /*} else if (alertConfiguration.getTriggerCondition().equals("logPattern")) {
	            // Filter log data by log pattern
	            if (logData.getMessage().contains(alertConfiguration.getLogPattern())) {
	                relevantLogs.add(logData);
	            }*/
	        }
	    }

	    return relevantLogs;
	}

	
	public  boolean evaluateConditions(AlertConfiguration alertConfiguration, List<Default> relevantLogs) {
	    // Implement your specific logic to evaluate conditions and criteria for triggering alerts
	    // For example, check the number of relevant logs, their timestamps, or any other relevant conditions

	    // Example logic for demonstration purposes:
	    int logCount = relevantLogs.size();
	    int thresholdValue = alertConfiguration.getThresholdValue();

	    // Trigger the alert if the log count exceeds the threshold value
	    return logCount > thresholdValue;
	}
	
	
	
	

}

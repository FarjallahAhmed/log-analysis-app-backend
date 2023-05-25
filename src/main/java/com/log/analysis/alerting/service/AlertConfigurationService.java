package com.log.analysis.alerting.service;

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
	
	private final AlertConfigurationRepository alertConfigurationRepository;

    @Autowired
    public AlertConfigurationService(AlertConfigurationRepository alertConfigurationRepository) {
        this.alertConfigurationRepository = alertConfigurationRepository;
    }

    public AlertConfiguration createAlertConfiguration(AlertConfiguration alertConfiguration) {
        return alertConfigurationRepository.save(alertConfiguration);
    }

    public List<AlertConfiguration> getAllAlertConfigurations() {
        return alertConfigurationRepository.findAll();
    }

    public AlertConfiguration getAlertConfigurationById(Long id) {
        return alertConfigurationRepository.findById(id).orElse(null);
    }

    public AlertConfiguration updateAlertConfiguration(Long id, AlertConfiguration updatedAlertConfiguration) {
        AlertConfiguration alertConfiguration = alertConfigurationRepository.findById(id).orElse(null);
        if (alertConfiguration != null) {
            alertConfiguration.setStatus(updatedAlertConfiguration.getStatus());
            // Update other fields as needed
            return alertConfigurationRepository.save(alertConfiguration);
        }
        return null;
    }

    public boolean deleteAlertConfiguration(Long id) {
        if (alertConfigurationRepository.existsById(id)) {
            alertConfigurationRepository.deleteById(id);
            return true;
        }
        return false;
    }
	
	public List<Default> getRelevantLogs(List<Default> logDataList) {
		
	    List<Default> relevantLogs = new ArrayList<>();	    
	    for (Default logData : logDataList) {
	    	
	            if (logData.getLoglevel().equals("ERROR")) {
	                relevantLogs.add(logData);
	            }
	        }

	    return relevantLogs;
	}
	
	public List<ExceptionDefault> getRelevantException(String message, List<ExceptionDefault> logDataList) {
		
	    List<ExceptionDefault> relevantLogs = new ArrayList<>();
	    
	    
	    for (ExceptionDefault logData : logDataList) {
	    	
	            if (logData.getErrorMessage().contains(message)) {
	                relevantLogs.add(logData);
	            }
	    }

	    return relevantLogs;
	}

	
	public  boolean evaluateConditions(AlertConfiguration alertConfiguration, List<?> relevantLogs) {
	    // Implement your specific logic to evaluate conditions and criteria for triggering alerts
	    // For example, check the number of relevant logs, their timestamps, or any other relevant conditions

	    // Example logic for demonstration purposes:
	    int logCount = relevantLogs.size();
	    int thresholdValue = alertConfiguration.getThresholdValue();

	    // Trigger the alert if the log count exceeds the threshold value
	    return logCount > thresholdValue;
	}
	
	
	
	

}

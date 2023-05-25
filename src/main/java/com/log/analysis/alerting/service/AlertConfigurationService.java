package com.log.analysis.alerting.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.log.analysis.alerting.repository.AlertConfigurationRepository;
import com.log.analysis.elasticsearch.model.AlertConfiguration;
import com.log.analysis.elasticsearch.model.Default;
import com.log.analysis.elasticsearch.model.ExceptionDefault;

@Service
public class AlertConfigurationService {
	
	private final AlertConfigurationRepository alertConfigurationRepository;
	private final JavaMailSender mailSender;

    @Autowired
    public AlertConfigurationService(AlertConfigurationRepository alertConfigurationRepository,
    								JavaMailSender mailSender) {
        this.alertConfigurationRepository = alertConfigurationRepository;
        this.mailSender = mailSender;
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
	    
		int logCount = relevantLogs.size();
	    int thresholdValue = alertConfiguration.getThresholdValue();

	    return logCount > thresholdValue;
	}
	
	public void sendEmailNotification(String recipientEmail, String subject, String content) {
		
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
	
	
	

}

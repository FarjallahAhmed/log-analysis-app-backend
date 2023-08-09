package com.log.analysis.alerting.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.log.analysis.alerting.repository.AlertConfigurationRepository;
import com.log.analysis.elasticsearch.AggregationLogsService;
import com.log.analysis.elasticsearch.model.AlertConfiguration;
import com.log.analysis.elasticsearch.model.Default;
import com.log.analysis.elasticsearch.model.ExceptionDefault;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class AlertConfigurationService {
	
	private final AlertConfigurationRepository alertConfigurationRepository;
	private final JavaMailSender mailSender;
	private final AggregationLogsService aggregationLogsService;

    @Autowired
    public AlertConfigurationService(AlertConfigurationRepository alertConfigurationRepository,
    								JavaMailSender mailSender,
    								AggregationLogsService aggregationLogsService) {
        this.alertConfigurationRepository = alertConfigurationRepository;
        this.mailSender = mailSender;
		this.aggregationLogsService = aggregationLogsService;
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

        // Get the current date to compare with log attributes
        LocalDate currentDate = LocalDate.now();

        for (Default logData : logDataList) {
            String logDateStr = logData.getDay() + " " + logData.getMonth() + " " + logData.getYear();
            LocalDate logDate = LocalDate.parse(logDateStr, DateTimeFormatter.ofPattern("d MMM yyyy"));

            // Check if the log entry is from the same day, month, and year as the current date
            if (logData.getLoglevel().equals("ERROR") && logDate.isEqual(currentDate)) {
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
	    boolean status = alertConfiguration.getStatus();

	    return (logCount > thresholdValue || status);
	}
	/*
	public void sendEmailNotification(String recipientEmail, String subject, String content,AlertConfiguration alert) {
		
		StringBuilder emailBody = new StringBuilder();
		emailBody.append("Alert Details:\n");
		emailBody.append("Alert Name: ").append(alert.getAlertName()).append("\n");
		emailBody.append("Trigger Condition: ").append(alert.getTriggerCondition()).append("\n");
		emailBody.append("Time Window: ").append(alert.getTimeWindow()).append("\n");
		emailBody.append("Alert Description: ").append(alert.getAlertDescription()).append("\n");
		emailBody.append("Escalation Level: ").append(alert.getEscalationLevel()).append("\n");
		
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(emailBody.toString());
        mailSender.send(message);
    }
	*/
	
	
	public void sendEmailNotification(String recipientEmail, String subject, String content, AlertConfiguration alert, Map<String, Object> summaryData) {
	    StringBuilder emailBody = new StringBuilder();
	    emailBody.append("<html><body>");

	    // Header
	    emailBody.append("<h2>Alert Details:</h2>");
	    emailBody.append("<ul>");
	    emailBody.append("<li><strong>Alert Name:</strong> ").append(alert.getAlertName()).append("</li>");
	    emailBody.append("<li><strong>Trigger Condition:</strong> ").append(alert.getTriggerCondition()).append("</li>");
	    emailBody.append("<li><strong>Time Window:</strong> ").append(alert.getTimeWindow()).append("</li>");
	    emailBody.append("<li><strong>Alert Description:</strong> ").append(alert.getAlertDescription()).append("</li>");
	    emailBody.append("<li><strong>Escalation Level:</strong> ").append(alert.getEscalationLevel()).append("</li>");
	    emailBody.append("</ul>");

	    // Additional content (you can customize this part)
	    emailBody.append("<p>").append(content).append("</p>");

	    // Additional section for service data summary in a table
	    emailBody.append("<h2>Service Data Summary:</h2>");
	    emailBody.append("<table border=\"1\">");
	    emailBody.append("<tr><th>Summary Item</th><th>Value</th></tr>");

	    emailBody.append("<tr><td>Total Logs</td><td>").append(summaryData.get("totalLogs")).append("</td></tr>");
	    emailBody.append("<tr><td>Error Logs</td><td>").append(summaryData.get("errorLogs")).append("</td></tr>");
	    emailBody.append("<tr><td>Latest Date</td><td>").append(summaryData.get("latestDate")).append("</td></tr>");
	    emailBody.append("<tr><td>Earliest Date</td><td>").append(summaryData.get("earliestDate")).append("</td></tr>");

	    emailBody.append("</table>");

	    // Additional section for log level percentages in a table
	    emailBody.append("<h2>Log Level Percentages:</h2>");
	    emailBody.append("<table border=\"1\">");
	    emailBody.append("<tr><th>Log Level</th><th>Percentage</th></tr>");
	    Map<String, Double> logLevelPercentages = (Map<String, Double>) summaryData.get("logLevelPercentages");
	    for (Map.Entry<String, Double> entry : logLevelPercentages.entrySet()) {
	        emailBody.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("%</td></tr>");
	    }
	    emailBody.append("</table>");

	    // Additional section for error message percentages in a table
	    emailBody.append("<h2>Error Message Percentages:</h2>");
	    emailBody.append("<table border=\"1\">");
	    emailBody.append("<tr><th>Error Message</th><th>Percentage</th></tr>");
	    Map<String, Double> errorMessagePercentages = (Map<String, Double>) summaryData.get("errorMessagePercentages");
	    for (Map.Entry<String, Double> entry : errorMessagePercentages.entrySet()) {
	        emailBody.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("%</td></tr>");
	    }
	    emailBody.append("</table>");

	    emailBody.append("</body></html>");

	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper helper;
	    try {
	        helper = new MimeMessageHelper(message, true);
	        helper.setTo(recipientEmail);
	        helper.setSubject(subject);
	        helper.setText(emailBody.toString(), true); // Set the body as HTML
	        mailSender.send(message);
	    } catch (MessagingException e) {
	        e.printStackTrace();
	    }
	}


	
	public void triggerAlerts(List<AlertConfiguration> alertConfigurations,
								List<Default> logs,
								List<ExceptionDefault> exception) throws IOException {
		
		Map<String, Object> summaryData;
		summaryData = aggregationLogsService.generateSummary("default_log_index");
        for (AlertConfiguration alertConfiguration : alertConfigurations) {
            if ("logLevel".equals(alertConfiguration.getTriggerCondition())) {
                List<Default> relevantLogs = getRelevantLogs(logs);
                boolean isAlertTriggered = evaluateConditions(alertConfiguration, relevantLogs);
                if (isAlertTriggered) {
                    sendEmailNotification("pfepfetest@gmail.com", "Alert Logs Errors", "alert of the logs ERROR.", alertConfiguration, summaryData);
                }
            } else if ("exception".equals(alertConfiguration.getTriggerCondition())) {
                List<ExceptionDefault> relevantLogs = getRelevantException(alertConfiguration.getTimeWindow(), exception);
                boolean isAlertTriggered = evaluateConditions(alertConfiguration, relevantLogs);
                if (isAlertTriggered) {
                    sendEmailNotification("pfepfetest@gmail.com", "Alert Logs Exception", "alert of the exception.", alertConfiguration, summaryData);
                }
            }
        }
    }	
	

}

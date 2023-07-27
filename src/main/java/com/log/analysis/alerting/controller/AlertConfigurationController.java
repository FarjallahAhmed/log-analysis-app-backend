package com.log.analysis.alerting.controller;

import java.io.IOException;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.log.analysis.alerting.repository.AlertConfigurationRepository;
import com.log.analysis.alerting.service.AlertConfigurationService;
import com.log.analysis.elasticsearch.GetDataService;
import com.log.analysis.elasticsearch.model.AlertConfiguration;
import com.log.analysis.elasticsearch.model.Default;
import com.log.analysis.elasticsearch.model.ExceptionDefault;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/alert-configurations")
public class AlertConfigurationController {

    private final AlertConfigurationRepository alertConfigurationRepository;
    private final GetDataService getService;
    private final AlertConfigurationService alertConfigurationService;

    @Autowired
    public AlertConfigurationController(AlertConfigurationRepository alertConfigurationRepository
    									,GetDataService getService
    									,AlertConfigurationService alertConfigurationService) {
        this.alertConfigurationRepository = alertConfigurationRepository;
		this.getService = getService;
		this.alertConfigurationService = alertConfigurationService;
    }

    @PostMapping
    public ResponseEntity<AlertConfiguration> createAlertConfiguration(@RequestBody AlertConfiguration alertConfiguration) {
        AlertConfiguration savedAlertConfiguration = alertConfigurationService.createAlertConfiguration(alertConfiguration);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAlertConfiguration);
    }

    @GetMapping
    public ResponseEntity<List<AlertConfiguration>> getAllAlertConfigurations() {
        List<AlertConfiguration> alertConfigurations = alertConfigurationService.getAllAlertConfigurations();
        return ResponseEntity.ok(alertConfigurations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertConfiguration> getAlertConfigurationById(@PathVariable Long id) {
        AlertConfiguration alertConfiguration = alertConfigurationService.getAlertConfigurationById(id);
        if (alertConfiguration != null) {
            return ResponseEntity.ok(alertConfiguration);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertConfiguration> updateAlertConfiguration(@PathVariable Long id, @RequestBody AlertConfiguration updatedAlertConfiguration) {
        AlertConfiguration alertConfiguration = alertConfigurationService.updateAlertConfiguration(id, updatedAlertConfiguration);
        if (alertConfiguration != null) {
            return ResponseEntity.ok(alertConfiguration);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlertConfiguration(@PathVariable Long id) {
        boolean deleted = alertConfigurationService.deleteAlertConfiguration(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/trigger")
    public ResponseEntity<List<Default>> triggerAlert() throws IOException {
    	
        List<AlertConfiguration> alertConfigurations = alertConfigurationRepository.findAll();
        List<Default> logs = this.getService.getLogs("default_log_index");
        List<ExceptionDefault> exception = this.getService.getException("default_log_index");
        
        this.alertConfigurationService.triggerAlerts(alertConfigurations,logs,exception);
        

        return new ResponseEntity<>(null, HttpStatus.OK);
    }


}

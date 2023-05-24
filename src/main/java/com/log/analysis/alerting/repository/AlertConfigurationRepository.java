package com.log.analysis.alerting.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.log.analysis.elasticsearch.model.AlertConfiguration;

public interface AlertConfigurationRepository extends JpaRepository<AlertConfiguration, Long>{


}

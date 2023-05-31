package com.log.analysis.elasticsearch.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportData {

	private int totalLogs;
    private int stackTraceLogs;
    private String latestDate;
    private String earliestDate;
    private int errorLogs;
    private List<String> topLoggers;
    private Map<String, Double> logLevelPercentages;
    private Map<String, Double> errorMessagePercentages;
}

package com.log.analysis.elasticsearch;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;


import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.awt.Color;
import java.io.*;
import java.text.SimpleDateFormat;


import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;





@Service
public class AggregationLogsService {
	
	
	@Autowired
	private RestHighLevelClient client;
	
	
	public Map<String, Long> getLogsPerMonth() throws IOException{
		
		Map<String, Long> results = new HashMap<>();
		
		SearchRequest searchRequest = new SearchRequest("default_log_index");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		searchSourceBuilder.size(0);
		
		searchSourceBuilder.aggregation(
				AggregationBuilders.dateHistogram("log_per_month")
					.field("@timestamp")
					.calendarInterval(DateHistogramInterval.MONTH)
				);
		
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		
		ParsedDateHistogram agg = searchResponse.getAggregations().get("log_per_month");
		for(Histogram.Bucket entry : agg.getBuckets()) {
			String key = entry.getKeyAsString();
			long docCount = entry.getDocCount();
			results.put(key, docCount);
			
		}
		
		
		return results;
	}

	public Map<String,Long> getTopMessage() throws IOException{
		
		Map<String, Long> messageCount = new HashMap<>(); 
		
		SearchRequest searchRequest = new SearchRequest("default_log_index");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		searchSourceBuilder.size(0);
		
		searchSourceBuilder.aggregation(AggregationBuilders.terms("message_count")
															.field("logmessage.keyword")
															.size(10));
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
			
		
		Terms messageCountsAgg = searchResponse.getAggregations().get("message_count");
		
		for(Terms.Bucket bucket : messageCountsAgg.getBuckets()) {
		
			messageCount.put(bucket.getKeyAsString(), bucket.getDocCount());
		}
				
		
		return messageCount;
	}

	public List<String> getDateRangeOfLogs() throws IOException {
		
		List<String> result = new ArrayList<>();
		
        SearchRequest searchRequest = new SearchRequest("default_log_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        DateRangeAggregationBuilder dateRangeAggregationBuilder = AggregationBuilders.dateRange("date_range")
                .field("@timestamp")
                .format("yyyy-MM-dd")
                .addRange("last_year", "now-1y/y", "now");
        searchSourceBuilder.aggregation(dateRangeAggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        
        Range dateRange = searchResponse.getAggregations().get("date_range");

        for(Range.Bucket bucket : dateRange.getBuckets() ) {
        	result.add(bucket.getFromAsString());
        	result.add(bucket.getToAsString());
        }
        
        return result;
    }
	
	public Map<String, Object> generateSummary() throws IOException {

	    Map<String, Object> summary = new HashMap<>();
	
	    // Get total count of logs
	    SearchRequest countRequest = new SearchRequest("default_log_index");
	    countRequest.source(new SearchSourceBuilder().size(0).query(QueryBuilders.matchAllQuery()));
	    SearchResponse countResponse = client.search(countRequest, RequestOptions.DEFAULT);
	    long totalLogs = countResponse.getHits().getTotalHits().value;
	    summary.put("totalLogs", totalLogs);
	
	    // Get count of logs with error messages
	    SearchRequest errorRequest = new SearchRequest("default_log_index");
	    errorRequest.source(new SearchSourceBuilder().size(0).query(QueryBuilders.existsQuery("ErrorMessage")));
	    SearchResponse errorResponse = client.search(errorRequest, RequestOptions.DEFAULT);
	    long errorLogs = errorResponse.getHits().getTotalHits().value;
	    summary.put("errorLogs", errorLogs);
	
	    // Get count of logs with stack traces
	    SearchRequest stackTraceRequest = new SearchRequest("default_log_index");
	    stackTraceRequest.source(new SearchSourceBuilder().size(0).query(QueryBuilders.existsQuery("StackTrace")));
	    SearchResponse stackTraceResponse = client.search(stackTraceRequest, RequestOptions.DEFAULT);
	    summary.put("stackTraceLogs", stackTraceResponse.getHits().getTotalHits().value);
	
	    // Get top 5 loggers by count
	    SearchRequest loggerRequest = new SearchRequest("default_log_index");
	    loggerRequest.source(new SearchSourceBuilder().size(0)
	            .aggregation(AggregationBuilders.terms("topLoggers").field("logger").size(5)));
	    SearchResponse loggerResponse = client.search(loggerRequest, RequestOptions.DEFAULT);
	    List<String> topLoggers = ((Terms) loggerResponse.getAggregations().get("topLoggers")).getBuckets()
	            .stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
	    summary.put("topLoggers", topLoggers);
	
	    // Get earliest and latest log dates
	    SearchRequest dateRequest = new SearchRequest("default_log_index");
	    dateRequest.source(new SearchSourceBuilder().size(0)
	            .aggregation(AggregationBuilders.min("earliestDate").field("log_date"))
	            .aggregation(AggregationBuilders.max("latestDate").field("log_date")));
	    SearchResponse dateResponse = client.search(dateRequest, RequestOptions.DEFAULT);
	    ZonedDateTime earliestDate = Instant.ofEpochMilli((long) ((Min) dateResponse.getAggregations().get("earliestDate")).getValue())
	            .atZone(ZoneId.systemDefault());
	    ZonedDateTime latestDate = Instant.ofEpochMilli((long) ((Max) dateResponse.getAggregations().get("latestDate")).getValue())
	            .atZone(ZoneId.systemDefault());
	    summary.put("earliestDate", earliestDate);
	    summary.put("latestDate", latestDate);
	    
	 // Get count of logs with each log level
	    SearchRequest logLevelRequest = new SearchRequest("default_log_index");
	    logLevelRequest.source(new SearchSourceBuilder().size(0)
	            .aggregation(AggregationBuilders.terms("logLevels").field("loglevel"))
	            .query(QueryBuilders.existsQuery("loglevel")));
	    SearchResponse logLevelResponse = client.search(logLevelRequest, RequestOptions.DEFAULT);
	    Map<String, Long> logLevelCounts = ((Terms) logLevelResponse.getAggregations().get("logLevels")).getBuckets()
	            .stream().collect(Collectors.toMap(Terms.Bucket::getKeyAsString, Terms.Bucket::getDocCount));
	    
	    
	    Map<String, Double> logLevelPercentages = new HashMap<>();
	    for (Map.Entry<String, Long> entry : logLevelCounts.entrySet()) {
	        logLevelPercentages.put(entry.getKey(), ((double) entry.getValue() / totalLogs) * 100);
	    }
	    summary.put("logLevelPercentages", logLevelPercentages);

	    // Get count of logs with each error message
	    SearchRequest errorMessageRequest = new SearchRequest("default_log_index");
	    errorMessageRequest.source(new SearchSourceBuilder().size(0)
	            .aggregation(AggregationBuilders.terms("errorMessages").field("ErrorMessage.keyword").size(10))
	            .query(QueryBuilders.existsQuery("ErrorMessage")));
	    SearchResponse errorMessageResponse = client.search(errorMessageRequest, RequestOptions.DEFAULT);
	    Map<String, Long> errorMessageCounts = ((Terms) errorMessageResponse.getAggregations().get("errorMessages")).getBuckets()
	            .stream().collect(Collectors.toMap(Terms.Bucket::getKeyAsString, Terms.Bucket::getDocCount));
	    Map<String, Double> errorMessagePercentages = new HashMap<>();
	    for (Map.Entry<String, Long> entry : errorMessageCounts.entrySet()) {
	        errorMessagePercentages.put(entry.getKey(), ((double) entry.getValue() / totalLogs) * 100);
	    }
	    summary.put("errorMessagePercentages", errorMessagePercentages);
	
	    return summary;
	}

    
    public  void generateReport(Map<String, Object> logData) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("LogReport.pdf"));
            document.open();
            
            // Add title
            Paragraph title = new Paragraph("Log Report");
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));
            
            // Add table for log data
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell(new PdfPCell(new Paragraph("Total Logs")));
            table.addCell(new PdfPCell(new Paragraph(logData.get("totalLogs").toString())));
            table.addCell(new PdfPCell(new Paragraph("Stack Trace Logs")));
            table.addCell(new PdfPCell(new Paragraph(logData.get("stackTraceLogs").toString())));
            table.addCell(new PdfPCell(new Paragraph("Latest Date")));
            table.addCell(new PdfPCell(new Paragraph(logData.get("latestDate").toString())));
            table.addCell(new PdfPCell(new Paragraph("Earliest Date")));
            table.addCell(new PdfPCell(new Paragraph(logData.get("earliestDate").toString())));
            table.addCell(new PdfPCell(new Paragraph("Error Logs")));
            table.addCell(new PdfPCell(new Paragraph(logData.get("errorLogs").toString())));
            table.addCell(new PdfPCell(new Paragraph("Top Loggers")));
            @SuppressWarnings("unchecked")
            List<String> topLoggers = (List<String>) logData.get("topLoggers");
            StringBuilder topLoggersStr = new StringBuilder();
            for (String logger : topLoggers) {
                topLoggersStr.append(logger).append(", ");
            }
            topLoggersStr.delete(topLoggersStr.length() - 2, topLoggersStr.length());
            table.addCell(new PdfPCell(new Paragraph(topLoggersStr.toString())));
            StringBuilder logLevelPercentages = new StringBuilder();
            for (String level : ((Map<String, Double>) logData.get("logLevelPercentages")).keySet()) {
                logLevelPercentages.append(level).append(": ")
                        .append(String.format("%.2f", ((Map<String, Double>) logData.get("logLevelPercentages")).get(level)))
                        .append("%, ");
            }
            logLevelPercentages.delete(logLevelPercentages.length() - 2, logLevelPercentages.length());
            table.addCell(new PdfPCell(new Paragraph(logLevelPercentages.toString())));
            table.addCell(new PdfPCell(new Paragraph("Error Message Percentages")));
            for (String message : ((Map<String, Double>) logData.get("errorMessagePercentages")).keySet()) {
                table.addCell(new PdfPCell(new Paragraph(message)));
                //table.addCell(new PdfPCell(new Paragraph(String.format("%.2f", ((Map<String, Double>) logData.get("errorMessagePercentages")).get(message) + "%"))));
            }
            document.add(table);
            
            document.close();
            System.out.println("Report generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

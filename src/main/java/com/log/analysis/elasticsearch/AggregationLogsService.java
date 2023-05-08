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

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;


import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileOutputStream;







@Service
public class AggregationLogsService {
	
	
	@Autowired
	private RestHighLevelClient client;
	
	private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font TEXT_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
	
	
	public Map<String, Long> getLogsPerMonth(String index) throws IOException{
		
		Map<String, Long> results = new HashMap<>();
		
		SearchRequest searchRequest = new SearchRequest(index);
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

	public Map<String,Long> getTopMessage(String index) throws IOException{
		
		Map<String, Long> messageCount = new HashMap<>(); 
		
		SearchRequest searchRequest = new SearchRequest(index);
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

	public List<String> getDateRangeOfLogs(String index) throws IOException {
		
		List<String> result = new ArrayList<>();
		
        SearchRequest searchRequest = new SearchRequest(index);
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
	
	
	public Map<String, Object> generateSummary(String index) throws IOException {

	    Map<String, Object> summary = new HashMap<>();
	
	    // Get total count of logs
	    SearchRequest countRequest = new SearchRequest(index);
	    countRequest.source(new SearchSourceBuilder().size(0).query(QueryBuilders.matchAllQuery()));
	    SearchResponse countResponse = client.search(countRequest, RequestOptions.DEFAULT);
	    long totalLogs = countResponse.getHits().getTotalHits().value;
	    summary.put("totalLogs", totalLogs);
	
	    // Get count of logs with error messages
	    SearchRequest errorRequest = new SearchRequest(index);
	    errorRequest.source(new SearchSourceBuilder().size(0).query(QueryBuilders.existsQuery("ErrorMessage")));
	    SearchResponse errorResponse = client.search(errorRequest, RequestOptions.DEFAULT);
	    long errorLogs = errorResponse.getHits().getTotalHits().value;
	    summary.put("errorLogs", errorLogs);
	
	    // Get count of logs with stack traces
	    SearchRequest stackTraceRequest = new SearchRequest(index);
	    stackTraceRequest.source(new SearchSourceBuilder().size(0).query(QueryBuilders.existsQuery("StackTrace")));
	    SearchResponse stackTraceResponse = client.search(stackTraceRequest, RequestOptions.DEFAULT);
	    summary.put("stackTraceLogs", stackTraceResponse.getHits().getTotalHits().value);
	
	    // Get top 5 loggers by count
	    SearchRequest loggerRequest = new SearchRequest(index);
	    loggerRequest.source(new SearchSourceBuilder().size(0)
	            .aggregation(AggregationBuilders.terms("topLoggers").field("logger").size(5)));
	    SearchResponse loggerResponse = client.search(loggerRequest, RequestOptions.DEFAULT);
	    List<String> topLoggers = ((Terms) loggerResponse.getAggregations().get("topLoggers")).getBuckets()
	            .stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
	    summary.put("topLoggers", topLoggers);
	
	    // Get earliest and latest log dates
	    SearchRequest dateRequest = new SearchRequest(index);
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
	    SearchRequest logLevelRequest = new SearchRequest(index);
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
	    SearchRequest errorMessageRequest = new SearchRequest(index);
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

    @SuppressWarnings("unchecked")
	public void generateReportPDF(Map<String, Object> logAnalysisReport, String filePath) throws DocumentException, IOException {
    
	    FileOutputStream outputStream = new FileOutputStream(filePath);
	    Document document = new Document();
	    PdfWriter.getInstance(document, outputStream);
	    document.open();
	
	    // Add Title
	    Paragraph title = new Paragraph("Log Analysis Report", TITLE_FONT);
	    title.setAlignment(Element.ALIGN_CENTER);
	    document.add(title);
	
	    // Add Total Logs
	    int totalLogs = (int) logAnalysisReport.get("totalLogs");
	    Paragraph totalLogsParagraph = new Paragraph("Total Logs: " + totalLogs, SUBTITLE_FONT);
	    document.add(totalLogsParagraph);
	
	    // Add Stack Trace Logs
	    int stackTraceLogs = (int) logAnalysisReport.get("stackTraceLogs");
	    Paragraph stackTraceLogsParagraph = new Paragraph("Exception: " + stackTraceLogs, SUBTITLE_FONT);
	    document.add(stackTraceLogsParagraph);
	
	    // Add Latest Date
	    String latestDate = (String) logAnalysisReport.get("latestDate");
	    Paragraph latestDateParagraph = new Paragraph("Latest log: " + latestDate, SUBTITLE_FONT);
	    document.add(latestDateParagraph);
	
	    // Add Earliest Date
	    String earliestDate = (String) logAnalysisReport.get("earliestDate");
	    Paragraph earliestDateParagraph = new Paragraph("Earliest log: " + earliestDate, SUBTITLE_FONT);
	    document.add(earliestDateParagraph);
	
	
	    // Add Log Level Percentages
	    Map<String, Double> logLevelPercentages = (Map<String, Double>) logAnalysisReport.get("logLevelPercentages");
	    Paragraph logLevelParagraph = new Paragraph("Log Level Percentages: ", SUBTITLE_FONT);
	    logLevelParagraph.setSpacingBefore(10);
	    logLevelParagraph.setSpacingAfter(10);
	    document.add(logLevelParagraph);
	    PdfPTable logLevelTable = new PdfPTable(2);
	    for (Map.Entry<String, Double> entry : logLevelPercentages.entrySet()) {
	        PdfPCell logLevelCell1 = new PdfPCell(new Phrase(entry.getKey(), TEXT_FONT));
	        PdfPCell logLevelCell2 = new PdfPCell(new Phrase(entry.getValue() + "%", TEXT_FONT));
	        logLevelTable.addCell(logLevelCell1);
	        logLevelTable.addCell(logLevelCell2);
	    }
	    document.add(logLevelTable);
	
	    // Add Error Message Percentages
	    Map<String, Double> errorMessagePercentages = (Map<String, Double>) logAnalysisReport.get("errorMessagePercentages");
	    Paragraph errorMessageParagraph = new Paragraph("Error Message Percentages: ", SUBTITLE_FONT);
	    errorMessageParagraph.setSpacingBefore(10);
	    errorMessageParagraph.setSpacingAfter(10);
	    document.add(errorMessageParagraph);
	    PdfPTable errorMessageTable = new PdfPTable(2);
	    for (Map.Entry<String, Double> entry : errorMessagePercentages.entrySet()) {
	        PdfPCell errorMessageCell1 = new PdfPCell(new Phrase(entry.getKey(), TEXT_FONT));
	        PdfPCell errorMessageCell2 = new PdfPCell(new Phrase(entry.getValue() + "%", TEXT_FONT));
	        errorMessageTable.addCell(errorMessageCell1);
	        errorMessageTable.addCell(errorMessageCell2);
	    }
	    document.add(errorMessageTable);
	
	    document.close();
	    outputStream.close();
	}

    
    
}

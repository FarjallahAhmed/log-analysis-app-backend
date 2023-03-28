package com.log.analysis.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.log.analysis.elasticsearch.model.Default;
import com.log.analysis.elasticsearch.model.ExceptionDefault;

@Service
public class GetDataService {
	
	@Autowired
	private RestHighLevelClient restClient;
	
	public List<Default> getSimpleLogs() throws IOException{
		
		SearchRequest searchRequest = new SearchRequest("default_log_index");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		searchSourceBuilder.size(1000);
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
		
		SearchHits hits = searchResponse.getHits();
		
		List<Default> simpleLogs = new ArrayList<>();
		
		for (SearchHit hit: hits) {

		    Default logs = new Default();

		    // Check if "loglevel" and "logger" keys exist and if they're not null before accessing them
		    if (hit.getSourceAsMap().containsKey("loglevel") && hit.getSourceAsMap().get("loglevel") != null
		            && hit.getSourceAsMap().containsKey("logger") && hit.getSourceAsMap().get("logger") != null) {
		    	
		        logs.setLogger(hit.getSourceAsMap().get("logger").toString());
		        logs.setLoglevel(hit.getSourceAsMap().get("loglevel").toString());
		        logs.setDay(hit.getSourceAsMap().get("day").toString());
		        logs.setMonth(hit.getSourceAsMap().get("month").toString());
		        logs.setYear(hit.getSourceAsMap().get("year").toString());
		        logs.setLogDate(hit.getSourceAsMap().get("log_date").toString());
		        logs.setLogMessage(hit.getSourceAsMap().get("logmessage").toString());
		        logs.setPackagee(hit.getSourceAsMap().get("package").toString());
		        logs.setThreadName(hit.getSourceAsMap().get("threadname").toString());
		        simpleLogs.add(logs);
		    }
		    
		}
		return simpleLogs;
	}
	
	public List<ExceptionDefault> getExceptionLogs() throws IOException{
		
		SearchRequest searchRequest = new SearchRequest("default_log_index");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		searchSourceBuilder.size(1000);
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
		
		SearchHits hits = searchResponse.getHits();
		
		
		List<ExceptionDefault> exceptionLogs = new ArrayList<>();
		
		for (SearchHit hit: hits) {

		    ExceptionDefault logs = new ExceptionDefault();
		    
		    // Check if "ErrorMessage" key exists and if it's not null before accessing it
		   if (hit.getSourceAsMap().containsKey("ErrorMessage") && hit.getSourceAsMap().get("ErrorMessage") != null) {
			   logs.setErrorMessage(hit.getSourceAsMap().get("ErrorMessage").toString());
			   String stackTrace = hit.getSourceAsMap().get("StackTrace").toString();
			   List<String> lines = new ArrayList<>(Arrays.asList(stackTrace.split("\\r\\n\\tat ")));
		       logs.setStackTrace(lines);
		       exceptionLogs.add(logs);
		   }
		    
		}
		return exceptionLogs;
	}

}

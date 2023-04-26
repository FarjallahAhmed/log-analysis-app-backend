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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;






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


















}

package com.log.analysis.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import com.log.analysis.elasticsearch.model.Default;

@Service
public class FilterLogsService {
	
	@Autowired
	RestHighLevelClient restClient;
	
	
	public List<Default> filterDefaultWithLogLevel(String level,String index) throws IOException{
		
		List<Default> logs = new ArrayList<>();
		
		SearchRequest searchRequest = new SearchRequest(index);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		searchSourceBuilder.query(QueryBuilders.matchQuery("loglevel",level));
		
		searchSourceBuilder.size(1000);
		
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse = restClient.search(searchRequest,RequestOptions.DEFAULT);
		
		SearchHits hits = searchResponse.getHits();
		
		for (SearchHit hit : hits) {

				Default log = new Default();

				log.setLogger(hit.getSourceAsMap().get("logger").toString());
				log.setLoglevel(hit.getSourceAsMap().get("loglevel").toString());
				log.setDay(hit.getSourceAsMap().get("day").toString());
				log.setMonth(hit.getSourceAsMap().get("month").toString());
				log.setYear(hit.getSourceAsMap().get("year").toString());
				log.setLogDate(hit.getSourceAsMap().get("log_date").toString());
				log.setLogMessage(hit.getSourceAsMap().get("logmessage").toString());
				log.setPackagee(hit.getSourceAsMap().get("package").toString());
				log.setThreadName(hit.getSourceAsMap().get("threadname").toString());
				logs.add(log);
			
		}
		
		return logs;
	}
	
	
	
	public List<Default> filterLogsByMessageKeyword (String message,String index) throws IOException{
		List<Default> logs = new ArrayList<>();
		
		SearchRequest searchRequest = new SearchRequest(index);
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		searchSourceBuilder.query(QueryBuilders.matchQuery("logmessage", message));
		searchSourceBuilder.size(1000);
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
		
		SearchHits hits = searchResponse.getHits();
		
		for (SearchHit hit : hits) {

			Default log = new Default();

			log.setLogger(hit.getSourceAsMap().get("logger").toString());
			log.setLoglevel(hit.getSourceAsMap().get("loglevel").toString());
			log.setDay(hit.getSourceAsMap().get("day").toString());
			log.setMonth(hit.getSourceAsMap().get("month").toString());
			log.setYear(hit.getSourceAsMap().get("year").toString());
			log.setLogDate(hit.getSourceAsMap().get("log_date").toString());
			log.setLogMessage(hit.getSourceAsMap().get("logmessage").toString());
			log.setPackagee(hit.getSourceAsMap().get("package").toString());
			log.setThreadName(hit.getSourceAsMap().get("threadname").toString());
			logs.add(log);
		
		}
		
		return logs;
	}
	
	
	public Map<String, Object> getLogsByDateRange(Date startDate, Date endDate, String index) throws IOException{
		
		Map<String, Object> results = new HashMap<>();
		
		SearchRequest searchRequest = new SearchRequest(index);
	    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

	    RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("@timestamp")
	            .gte(startDate.getTime())
	            .lte(endDate.getTime());
	    
	    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
	            .must(rangeQuery)
	            .must(QueryBuilders.existsQuery("log_date"));

	    searchSourceBuilder.query(boolQuery);
	    searchSourceBuilder.size(10000);
	    
	    searchRequest.source(searchSourceBuilder);
	    
	    SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
	    
	    results.put("totalHits", searchResponse.getHits().getTotalHits().value);
	    
	    results.put("logs", Arrays.stream(searchResponse.getHits().getHits())
	            .map(SearchHit::getSourceAsMap)
	            .collect(Collectors.toList()));
	    
		return results;
	}
	

}

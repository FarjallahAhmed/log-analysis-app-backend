package com.log.analysis.elasticsearch.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionDefault {

	private String errorMessage;
	private List<String> stackTrace;
}

package com.sulistionoadi.ngoprek.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class DefaultResponse {
	
	private String code;
	private String message;
	private Object data;

	@Builder
	public DefaultResponse(String code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
}

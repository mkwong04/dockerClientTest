package rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RegisterRequest {
	
	@JsonProperty
	private String userName;
	
	@JsonProperty
	private String appName;
}

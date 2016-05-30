package rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserApp{
	
	@JsonProperty
	private String id;
	
	@JsonProperty
	private String userName;
	
	@JsonProperty
	private String appName;
	
	@JsonProperty
	private String displayName;
	
	@JsonProperty
	private String slogn;
	
	@JsonProperty
	private String description;
	
	@JsonProperty
	private String features;
}
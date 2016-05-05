package rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
	
	@JsonProperty
	private String status;
	
	@JsonProperty
	private String url;
}

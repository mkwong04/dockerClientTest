package rest.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAppListResponse{
	
	@JsonProperty
	private List<UserApp> userAppList = new ArrayList<>();
}
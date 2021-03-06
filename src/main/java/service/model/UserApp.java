package service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserApp{
	
	private String id;
	private String userName;
	private String appName;
	
	//used for internal apache reversed proxy route config generation
	private String containerUrl;
	private String containerName;
	
	//for ui usage
	private String displayName;
	private String slogan;
	private String description;
	private String features;
}
package service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserApp{
	
	private String id;
	private String userName;
	private String appName;
	
	private String containerUrl;
	private String containerName;
	
	private String networkId;
}
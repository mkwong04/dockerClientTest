package service;

public interface DockerService{
	
	/**
	 * 
	 * @param containerName
	 * @param appImageName
	 * @param startCmd
	 * @param containerListenUrlPattern
	 * @return
	 */
	String createApp(String containerName, 
					 String appImageName, 
					 String startCmd,
					 String containerListenUrlPattern);
}
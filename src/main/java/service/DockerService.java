package service;

import service.exception.DockerServiceException;

public interface DockerService{
	
	/**
	 * 
	 * @param containerName
	 * @param appImageName
	 * @param containerListenUrlPattern
	 * @param startCmd
	 * @return
	 */
	String createApp(String containerName, 
					 String appImageName,
					 String containerListenUrlPattern,
					 String... startCmd)
		throws DockerServiceException;
	
	/**
	 * 
	 * @param networkName
	 * @param driver
	 * @return
	 * @throws DockerServiceException
	 */
	String createConnection(String networkName, 
			 				String driver)
		throws DockerServiceException;
	
	/**
	 * 
	 * @param networkId
	 * @param containerName
	 * @return
	 * @throws DockerServiceException
	 */
	void connectConnection(String networkId, 
		                   String containerName)
		throws DockerServiceException;
	
	/**
	 * 
	 * @param sourceContainerName
	 * @param sourcePath
	 * @param targetContainerName
	 * @param targetPath
	 */
	void copyFile(String sourceContainerName,
				  String sourcePath,
				  String targetContainerName,
				  String targetPath);
	
	/**
	 * 
	 * @param containerName
	 * @param cmd
	 * @throws DockerServiceException
	 */
	void execCmd(String containerName, String... cmd) throws DockerServiceException;
}
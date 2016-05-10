package service;

import service.exception.DockerServiceException;

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
	String execCmd(String containerName, String cmd) throws DockerServiceException;
}
package service;

import java.util.Optional;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.NetworkSettings;

import service.exception.DockerServiceException;

public interface DockerService{
	
	/**
	 * 
	 * @param containerName
	 * @param appImageName
	 * @param startCmd
	 * @return
	 */
	String createApp(String containerName, 
					 String appImageName,
					 String... startCmd)
		throws DockerServiceException;
	
	/**
	 * 
	 * @param containerId
	 * @throws DockerServiceException
	 */
	void removeApp(String containerId)
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
	 * @throws DockerServiceException
	 */
	void removeConnection(String networkId)
			throws DockerServiceException;
	/**
	 * 
	 * @param networkId
	 * @param containerNameOrId
	 * @return
	 * @throws DockerServiceException
	 */
	void connectConnection(String networkId, 
		                   String containerNameOrId)
		throws DockerServiceException;
	
	/**
	 * 
	 * @param networkId
	 * @param containerId
	 * @throws DockerServiceException
	 */
	void disconnectConnection(String networkId, String containerId) 
		throws DockerServiceException;
	
	/**
	 * 
	 * @param newtworkName
	 * @param containerId
	 * @return
	 * @throws DockerServiceException
	 */
	Optional<String> getConnectionId(String newtworkName, String containerId) throws DockerServiceException;
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
	
	/**
	 * 
	 * @param containerName
	 * @return
	 * @throws DockerServiceException
	 */
	Optional<Container> getContainer(String containerName) throws DockerServiceException;
	
	/**
	 * 
	 * @param containerName
	 * @return
	 * @throws DockerServiceException
	 */
	Optional<String> getContainerId(String containerName) throws DockerServiceException;
	
	/**
	 * 
	 * @param containerId
	 * @param networkName
	 * @return
	 * @throws DockerServiceException
	 */
	Optional<NetworkSettings.Network> getContainerNetwork(String containerId, String networkName) 
			throws DockerServiceException;
}
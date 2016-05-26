package service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;

import lombok.extern.slf4j.Slf4j;
import service.DockerService;
import service.exception.DockerServiceException;

@Slf4j
public class DockerServiceDockerJavaImpl implements DockerService{
	
	@Value("${docker.url}")
	private String dockerUrl;
	
	@Value("${file.dir}")
	private String fileDir;
	
	@Value("${apache.conf.name}")
	private String apacheConfFile;
	
	@Override
	public String createApp(String containerName, 
							String appImageName, 
							String... startCmd) 
		throws DockerServiceException{
		
		log.info("dockerUrl : {}",dockerUrl);
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
		
			log.info("creating container from image : {}",appImageName);
			
			CreateContainerResponse container = dockerClient.createContainerCmd(appImageName)
														    .withName(containerName)
														    .withAttachStdin(true)
														    .withTty(true)
														    .exec();
			
			log.info("starting container : {}",container.getId());
			dockerClient.startContainerCmd(container.getId()).exec();
			
			execCmd(containerName, null, startCmd);
			
			return container.getId();
		}
		catch(IOException e){
			throw new DockerServiceException("failed create App",e);
		}
	}
	
	@Override
	public void removeApp(String containerId)
		throws DockerServiceException{
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			
			log.info("stopping container : {}",containerId);
			
			dockerClient.stopContainerCmd(containerId)
						//if 60sec grace period exceed for gracefully stop container, kill the container
						.withTimeout(60)
						.exec();
			
			log.info("removing container : {}", containerId);
			dockerClient.removeContainerCmd(containerId)
						.exec();
			
		}
		catch(IOException e){
			throw new DockerServiceException("failed removing container "+containerId,e);
		}
	}
	@Override
	public String createConnection(String networkName, 
								   String driver)
		throws DockerServiceException{
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			
			CreateNetworkResponse response = dockerClient.createNetworkCmd()
														 .withName(networkName)
														 .withDriver(driver)
														 .exec();
			
			log.info("network id :{}",response.getId());
			
			return response.getId();
		} 
		catch (IOException e) {
			throw new DockerServiceException("create connection failed",e);
		}
	}
	
	@Override
	public void removeConnection(String networkId)
			throws DockerServiceException{
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			dockerClient.removeNetworkCmd(networkId)
						.exec();
		} 
		catch (IOException e) {
			throw new DockerServiceException("remove connection failed",e);
		}
	}
	
	@Override
	public Optional<String> getContainerId(String containerName) throws DockerServiceException{
		Optional<Container> container = getContainer(containerName);
		
		if(container.isPresent()){
			return Optional.ofNullable(container.get().getId());
		}
		else{
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<Container> getContainer(String containerName) throws DockerServiceException{
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			String refContainerName = "/"+containerName;
			List<Container> containers = dockerClient.listContainersCmd().exec();
			
			return containers.stream()
					  		 .filter(obj -> {
					  			 			log.info("container name : {}",Arrays.asList(obj.getNames()));
					  			 			
					  			 			return obj.getNames()!=null && 
					  			 				   obj.getNames().length>0 && 
					  			 				   refContainerName.equals(obj.getNames()[0]);})
					  		 .findFirst();
		}
		catch (IOException e) {
			throw new DockerServiceException("get container by name failed",e);
		}
	}
	
	@Override
	public Optional<NetworkSettings.Network> getContainerNetwork(String containerId, String networkName) 
			throws DockerServiceException{
		Map<String, NetworkSettings.Network> resultMap = getContainerNetworks(containerId);
		
		NetworkSettings.Network network = resultMap.get(networkName);
		
		if(network==null){
			return Optional.empty();
		}
		else{
			return Optional.of(network);
		}
	}
	
	public Map<String, NetworkSettings.Network> getContainerNetworks(String containerId) throws DockerServiceException{
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			InspectContainerResponse result = dockerClient.inspectContainerCmd(containerId)
														  .exec();
			
			return result.getNetworkSettings().getNetworks();
		}
		catch (IOException e) {
			throw new DockerServiceException("inspect container by name failed",e);
		}
	}
	
	@Override
	public Optional<String> getConnectionId(String newtworkName, 
											String containerId) 
		throws DockerServiceException{
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			
				List<Network> networks = dockerClient.listNetworksCmd()
													 .withNameFilter(newtworkName)
													 .exec();
				
				if(!networks.isEmpty()){
					//since network name is not guarantee unique, use containerId to ensure picking up the right network
					
					Optional<Network> networkOpt = networks.stream()
														   .filter(obj-> obj.getContainers()!=null && 
															   		     obj.getContainers().get(containerId) !=null)
														   .findFirst();
					
					if(networkOpt.isPresent()){
						return Optional.ofNullable(networkOpt.get().getId());
					}
					else{
						return Optional.empty();
					}
				}
				else{
					return Optional.empty();
				}
		}
		catch (IOException e) {
			throw new DockerServiceException("get connection by name failed",e);
		}
	}
	
	@Override
	public void disconnectConnection(String networkId, String containerId) throws DockerServiceException{
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){

			dockerClient.disconnectFromNetworkCmd()
						.withContainerId(containerId)
						.withNetworkId(networkId)
						.exec();
		}
		catch (IOException e) {
			throw new DockerServiceException("disconnection connection failed",e);
		}
	}
	
	@Override
	public void connectConnection(String networkId, 
								  String containerNameOrId)
		throws DockerServiceException{
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			dockerClient.connectToNetworkCmd()
						.withNetworkId(networkId)
						.withContainerId(containerNameOrId)
						.exec();
		}
		catch (IOException e) {
			throw new DockerServiceException("connection connection failed",e);
		}
	}

	@Override
	public void copyFile(String sourceContainerName, 
						 String sourcePath, 
						 String targetContainerName, 
						 String targetPath) {
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();
			BufferedInputStream bis = new BufferedInputStream(dockerClient.copyArchiveFromContainerCmd(sourceContainerName, sourcePath).exec());){
			
			Path outputPath = Paths.get(fileDir+File.separator+apacheConfFile);
			//write to host system 
			Files.copy(bis, outputPath, StandardCopyOption.REPLACE_EXISTING);			
			
			dockerClient.copyArchiveToContainerCmd(targetContainerName)
						.withRemotePath(targetPath)
						.withHostResource(fileDir+File.separator+apacheConfFile)
						.exec();
		}
		catch(IOException e){
			log.error("Failed create docker client",e);
		}
	}
	
	@Override
	public void execCmd(String containerName, String... cmd) throws DockerServiceException{
		
		execCmd(containerName, 1L, cmd);
	}
	
	private void execCmd(String containerName, 
						 Long timeoutMins, 
						 String... cmd) throws DockerServiceException{
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			ExecCreateCmdResponse response = dockerClient.execCreateCmd(containerName)
														 .withCmd(cmd)
														 .withAttachStdin(false)
														 .withAttachStdout(true)
														 .withAttachStderr(true)
														 .exec();
			
			log.info("exec id :{}",response.getId());
			
			ExecStartResultCallback callBack  = dockerClient.execStartCmd(response.getId())
														    .withDetach(false)
														    .withTty(false)
														    .exec(new ExecStartResultCallback());
			
			if(timeoutMins!=null){
				if(!callBack.awaitCompletion(1, TimeUnit.MINUTES)){
					throw new DockerServiceException("time out while execute command "+cmd);
				}
			}
			else{
				callBack.awaitStarted();
			}
			
		}
		catch(IOException | InterruptedException e){
			throw new DockerServiceException(e);
		}
	}
	
	
	private DockerClientConfig createConfig(){
		return DockerClientConfig.createDefaultConfigBuilder()
								  .withDockerHost(dockerUrl)
								  .withDockerTlsVerify(false)
								  .withApiVersion("1.21")
								  .build();
	}

	
}
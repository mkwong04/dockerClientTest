package service.impl;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.NetworkSettings.Network;
import com.github.dockerjava.core.DockerClientBuilder;

import service.DockerService;

@Slf4j
public class DockerServiceDockerJavaImpl implements DockerService{
	
	@Value("${docker.url}")
	private String dockerUrl;

	@Override
	public String createApp(String containerName, 
							String appImageName, 
							String startCmd,
							String containerListenUrlPattern) {
		String userAppUrl = null;
		
		log.info("dockerUrl : {}",dockerUrl);
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(dockerUrl).build();){
		
			log.info("creating container from image : {}",appImageName);
			
			CreateContainerResponse container = dockerClient.createContainerCmd(appImageName)
														    .withCmd(startCmd)
														    .withName(containerName)
														    .exec();
			
			log.info("starting container : {}",container.getId());
			dockerClient.startContainerCmd(container.getId()).exec();
			
			log.info("inspecting container");
			InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
			
			Network network = inspectResponse.getNetworkSettings().getNetworks().get("bridge");
			
			userAppUrl = String.format(containerListenUrlPattern, network.getIpAddress());
			
			log.info("redirect target url :{}",userAppUrl);
			
		}
		catch(IOException e){
			log.error("Failed create docker client",e);
		}
		
		return userAppUrl;
	}
	
}
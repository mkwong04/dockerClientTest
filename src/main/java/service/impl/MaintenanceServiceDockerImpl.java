package service.impl;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import service.MaintenanceService;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.core.DockerClientBuilder;

@Slf4j
public class MaintenanceServiceDockerImpl implements MaintenanceService {
	
	@Value("docker.url")
	private String dockerUrl;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createApp(String userName, String appImageName){
		String userAppUrl = null;
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(dockerUrl).build();){
		
			log.info("creating container from image : {}",appImageName);
			
			CreateContainerResponse container = dockerClient.createContainerCmd(appImageName)
														    .withCmd("/home/usr/ribbitup/SandstormPlaySampleTest/start")
														    .withName(userName)
														    .exec();
			
			log.info("finding the virtual IP of container");
			InspectContainerResponse inspectResult = dockerClient.inspectContainerCmd(container.getId()).exec();
			
			String containerVirtualIpAddress = inspectResult.getNetworkSettings().getIpAddress();
			log.info("virtual IP of container : {}",containerVirtualIpAddress);
			
			userAppUrl = String.format("http://$s:9001", containerVirtualIpAddress);
			
			log.info("redirect target url :{}",userAppUrl);
			dockerClient.startContainerCmd(container.getId()).exec();
		}
		catch(IOException e){
			log.error("Failed create docker client",e);
		}
		
		return userAppUrl;
	}
}

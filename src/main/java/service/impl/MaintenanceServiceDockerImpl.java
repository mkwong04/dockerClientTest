package service.impl;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import service.MaintenanceService;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.DockerClientBuilder;

@Slf4j
public class MaintenanceServiceDockerImpl implements MaintenanceService {
	
	@Value("${docker.url}")
	private String dockerUrl;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createApp(String userName, String appImageName){
		String userAppUrl = null;
		
		log.info("dockerUrl : {}",dockerUrl);
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(dockerUrl).build();){
		
			log.info("creating container from image : {}",appImageName);
			
			CreateContainerResponse container = dockerClient.createContainerCmd(appImageName)
															.withHostName(userName)
														    .withCmd("/home/usr/ribbitup/start")
														    .withName(userName)
														    .exec();
			
			
			userAppUrl = String.format("http://%s:9001", userName);
			
			log.info("redirect target url :{}",userAppUrl);
			dockerClient.startContainerCmd(container.getId()).exec();
		}
		catch(IOException e){
			log.error("Failed create docker client",e);
		}
		
		return userAppUrl;
	}
}

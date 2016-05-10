package service.impl;

import java.util.Properties;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import service.ApacheConfGenService;
import service.DockerService;
import service.MaintenanceService;
import service.UserAppService;
import service.exception.ApacheConfGenServiceException;
import service.exception.MaintenanceServiceException;
import service.exception.UserAppServiceException;
import service.model.UserApp;

@Slf4j
public class MaintenanceServiceImpl implements MaintenanceService{
	
	@Resource
	@Qualifier("appImagesProperties")
	private Properties appImagesProperties;
	
	@Value("${domain.url}")
	private String domainUrl;
	
	@Autowired
	private DockerService dockerService;
	
	@Autowired
	private UserAppService userAppService;
	
	@Autowired
	private ApacheConfGenService apacheConfGenService;

	@Override
	public String createApp(String userName, String appName) throws MaintenanceServiceException{
		
		String containerName = userName+"_"+appName;
		
		//TODO:
		String appImageName = appImagesProperties.getProperty(appName);
		String startCmd = "/home/usr/ribbitup/start";
		String containerListenUrlPattern = "http://%s:9001";
		
		if(appImageName==null || appImageName.trim() ==""){
			throw new MaintenanceServiceException("no images found for "+appName);
		}
		
		
		log.info("creating docker container");
		//1. docker remote API to create new container by image
		String routeUrl = dockerService.createApp(containerName, 
												  appImageName, 
												  startCmd,
												  containerListenUrlPattern);
		
		UserApp userApp = UserApp.builder()
								 .id("0")
								 .userName(userName)
								 .appName(appName)
								 .containerName(containerName)
								 .containerUrl(routeUrl)
								 .build();
		
		//2. create entry in persistence layer
		log.info("Saving record to persistence layer");
		try {
			userApp = userAppService.create(userApp);
		} 
		catch (UserAppServiceException e) {
			throw new MaintenanceServiceException(e);
		}
		
		//3. generate the apache2 conf
		log.info("generate apache2 conf");
		try {
			apacheConfGenService.genConfig();
		} 
		catch (ApacheConfGenServiceException e) {
			throw new MaintenanceServiceException(e);
		}
		
		//4. copy to apache2 container to override and reload conf
		return String.format("%s/%s", domainUrl, userApp.getContainerName());
	}
	
}
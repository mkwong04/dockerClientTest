package service.impl;

import java.io.File;
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
import service.exception.DockerServiceException;
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
	
	@Value("${maintenance.container.name}")
	private String maintenanceContainerName;
	
	@Value("${apache.container.name}")
	private String apacheContainerName;
	
	@Value("${file.dir}")
	private String maintenanceFileDir;
	
	@Value("${apache.conf.name}")
	private String apacheConfFile;
	
	@Value("${apache.container.conf.path}")
	private String apacheContainerConfPath;
	
	@Value("${apache.container.default.conf.name}")
	private String apacheContainerDefaultConfName;
	
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
		String defaultCmd = "/bin/bash";
		String startCmd = "/home/usr/ribbitup/start";
		String containerListenUrlPattern = "http://%s:9001";
		
		if(appImageName==null || appImageName.trim() ==""){
			throw new MaintenanceServiceException("no images found for "+appName);
		}
		
		
		log.info("creating docker container");
		//1. docker remote API to create new container by image
		String routeUrl = dockerService.createApp(containerName, 
												  appImageName, 
												  defaultCmd,
												  containerListenUrlPattern);
		
		try {
			dockerService.execCmd(containerName, defaultCmd, "-c", startCmd);
		} 
		catch (DockerServiceException e1) {
			throw new MaintenanceServiceException("Failed starting container ["+containerName+"]",e1);
		}
		
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
		dockerService.copyFile(maintenanceContainerName, 
							   maintenanceFileDir+File.separator+apacheConfFile, 
							   apacheContainerName, 
							   apacheContainerConfPath);
		
		try {
			
			dockerService.execCmd(apacheContainerName, 
								  "/bin/bash", 
								  "-c", 
								  "tar -xf "+apacheContainerConfPath+File.separator+apacheConfFile);
			log.info("tar extracted");
			
			
			//backup
			dockerService.execCmd(apacheContainerName,
								  "/bin/bash", 
								  "-c", 
								   "cp "+apacheContainerConfPath+File.separator+apacheContainerDefaultConfName+" "
									   +apacheContainerConfPath+File.separator+apacheContainerDefaultConfName+".bak");
			log.info("backup existing config");
			//replace
			dockerService.execCmd(apacheContainerName, 
								  "/bin/bash", 
								  "-c", 
								  "cp "+apacheContainerConfPath+File.separator+apacheConfFile+" "
									   +apacheContainerConfPath+File.separator+apacheContainerDefaultConfName);
			
			log.info("replace config");
			//reload apache conf
			dockerService.execCmd(apacheContainerName,
								  "/bin/bash", 
								  "-c",
								  "service apache2 reload");
			log.info("reload config");

		} 
		catch (DockerServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return String.format("%s/%s", domainUrl, userApp.getContainerName());
	}
	
}
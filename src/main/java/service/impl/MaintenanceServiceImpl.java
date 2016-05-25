package service.impl;

import static app.Constant.START_CMD_POSTFIX;
import static app.Constant.START_URL_PATTERN_POSTFIX;

import java.io.File;
import java.util.List;
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
	
	static final String BASH_CMD = "/bin/bash";
	static final String BASH_STRING_OPT = "-c";
	
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

		String startCmd = appImagesProperties.getProperty(appName+START_CMD_POSTFIX);
		String containerListenUrlPattern = appImagesProperties.getProperty(appName+START_URL_PATTERN_POSTFIX);
		
		if(appImageName==null || appImageName.trim() ==""){
			throw new MaintenanceServiceException("no images found for "+appName);
		}
		
		
		try
		{
			log.info("creating docker container");
			//1. docker remote API to create new container by image
			String appContainerId = dockerService.createApp(containerName, 
													  		appImageName,
													  		construnctCmd(startCmd));
		
			String routeUrl = String.format(containerListenUrlPattern, containerName);
			
			log.info("redirect target url :{}",routeUrl);
			//2. create user defined network bridge
			log.info("Creating network bridge for {}",containerName);
			String networkId = dockerService.createConnection(containerName, 
															  "bridge");
			
			//3. create entry in persistence layer
			UserApp userApp = UserApp.builder()
					 				 .id("0")
					 				 .userName(userName)
					 				 .appName(appName)
					 				 .containerName(containerName)
					 				 .containerUrl(routeUrl)
					 				 .networkId(networkId)
					 				 .build();

			log.info("Saving record to persistence layer");
			userApp = userAppService.create(userApp);
			
			//4. connect network bridge to app container
			log.info("Connecting network bridge {} to {} ", networkId, appContainerId);
			dockerService.connectConnection(networkId, appContainerId);
			
			//5. connect network bridge to apache container
			log.info("Connecting network bridge {} to {} ", networkId, apacheContainerName);
			dockerService.connectConnection(networkId, apacheContainerName);
			 
			//6. generate the apache2 conf
			log.info("generate apache2 conf");
			apacheConfGenService.genConfig();
			
			//7. copy to apache2 container to override and reload conf
			dockerService.copyFile(maintenanceContainerName, 
								   maintenanceFileDir+File.separator+apacheConfFile, 
								   apacheContainerName, 
								   apacheContainerConfPath);
			
			dockerService.execCmd(apacheContainerName, 
								  construnctCmd("tar -xOf "+apacheContainerConfPath+File.separator+apacheConfFile+
										  		" > "+
										  		apacheContainerConfPath+File.separator+apacheConfFile+".plain"));
			log.info("tar extracted");
			
			
			//8. backup
			dockerService.execCmd(apacheContainerName,
								  construnctCmd(
								   "cp "+apacheContainerConfPath+File.separator+apacheContainerDefaultConfName+" "
									   +apacheContainerConfPath+File.separator+apacheContainerDefaultConfName+".bak"));
			log.info("backup existing config");
			//9. replace
			dockerService.execCmd(apacheContainerName, 
								  construnctCmd(
								  "cp "+apacheContainerConfPath+File.separator+apacheConfFile+".plain "
									   +apacheContainerConfPath+File.separator+apacheContainerDefaultConfName));
			
			log.info("replace config");
			//10. reload apache conf
			dockerService.execCmd(apacheContainerName,
								  construnctCmd("service apache2 reload"));
			log.info("reload config");
			
			return String.format("%s/%s/", domainUrl, userApp.getContainerName());
			
		}
		catch (DockerServiceException e1) {
			throw new MaintenanceServiceException("Failed starting container ["+containerName+"]",e1);
		}
		catch (UserAppServiceException | ApacheConfGenServiceException e) {
			throw new MaintenanceServiceException(e);
		}

	}
	
	private String[] construnctCmd(String cmd){
		
		log.info(BASH_CMD+" "+BASH_STRING_OPT+" {}", cmd);
		return new String[]{BASH_CMD, BASH_STRING_OPT, cmd};
	}

	@Override
	public List<UserApp> listAllUserApp() throws MaintenanceServiceException {
		try{
			return userAppService.findAll();
		}
		catch(UserAppServiceException e){
			throw new MaintenanceServiceException(e);
		}
	}

	@Override
	public List<UserApp> findUserApp(String userName) throws MaintenanceServiceException {
		try{
			return userAppService.findByUserName(userName);
		}
		catch(UserAppServiceException e){
			throw new MaintenanceServiceException(e);
		}
	}
}
package service.impl;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.NetworkSettings;

import lombok.extern.slf4j.Slf4j;
import service.ApacheConfGenService;
import service.AppConfigService;
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
	
	static final String NETWORK_PREFIX = "nb";
	static final String BASH_CMD = "/bin/bash";
	static final String BASH_STRING_OPT = "-c";
	
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
	
	@Autowired
	private AppConfigService appConfigService;

	@Override
	public String createApp(String userName, String appName) throws MaintenanceServiceException{
		
		String containerName = userName+"_"+appName;
		
		String appImageName = appConfigService.getImageName(appName);
		String startCmd = appConfigService.getStartCommand(appName);
		String containerListenUrlPattern = appConfigService.getStartUrlPattern(appName);
		
		if(appImageName==null || appImageName.trim() ==""){
			throw new MaintenanceServiceException("no images defined for "+appName);
		}
		
		try
		{
			log.info("creating docker container");
			//1. docker remote API to create new container by image
			String appContainerId = dockerService.createApp(containerName, 
													  		appImageName,
													  		constructCmd(startCmd));
		
			String routeUrl = String.format(containerListenUrlPattern, containerName);
			
			log.info("redirect target url :{}",routeUrl);
			//2. create user defined network bridge
			//NOTE: added network prefix "nb" as some network bridge name cause apache2 to not responding
			String networkId = createContainerNetwork(NETWORK_PREFIX+containerName, appContainerId);
			log.info("network bridge created :{}",networkId);

			//3. create entry in persistence layer
			UserApp userApp = UserApp.builder()
					 				 .id("0")
					 				 .userName(userName)
					 				 .appName(appName)
					 				 .containerName(containerName)
					 				 .containerUrl(routeUrl)
					 				 .displayName(appConfigService.getDisplayName(appName))
					 				 .slogan(appConfigService.getSlogan(appName))
					 				 .description(appConfigService.getDescription(appName))
					 				 .features(appConfigService.getFeatures(appName))
					 				 .build();

			log.info("Saving record to persistence layer");
			userApp = userAppService.create(userApp);
			 
			//4. rebuild apache routing config from persisted userApps
			updateApacheRouteConfig();
			
			return String.format("%s/%s/", domainUrl, userApp.getContainerName());
			
		}
		catch (DockerServiceException e1) {
			throw new MaintenanceServiceException("Failed starting container ["+containerName+"]",e1);
		}
		catch (UserAppServiceException e) {
			throw new MaintenanceServiceException(e);
		}
	}
	
	/**
	 * 
	 * @param networkName
	 * @param appContainerId
	 * @return
	 * @throws MaintenanceServiceException
	 */
	private String createContainerNetwork(String networkName, String appContainerId) 
			throws MaintenanceServiceException{
		try{
			//1. create network bridge connection
			log.info("Creating network bridge for {}",networkName);
			String networkId = dockerService.createConnection(networkName, 
															  "bridge");
			
			//2. connect network bridge to app container
			log.info("Connecting network bridge {} to {} ", networkId, appContainerId);
			dockerService.connectConnection(networkId, appContainerId);
			
			//5. connect network bridge to apache container
			log.info("Connecting network bridge {} to {} ", networkId, apacheContainerName);
			dockerService.connectConnection(networkId, apacheContainerName);
			
			return networkId;
		}
		catch(DockerServiceException e){
			throw new MaintenanceServiceException("Failed create container network for "+networkName,e);
		}
	}
	
	/**
	 * 
	 * @throws MaintenanceServiceException
	 */
	private void updateApacheRouteConfig() throws MaintenanceServiceException{
		try{
			//1. generate the apache2 conf base on persisted data
			log.info("generate apache2 conf");
			apacheConfGenService.genConfig();
			
			//2. copy to apache2 container to override and reload conf
			dockerService.copyFile(maintenanceContainerName, 
								   maintenanceFileDir+File.separator+apacheConfFile, 
								   apacheContainerName, 
								   apacheContainerConfPath);
			
			//3. extract tar to plain form
			dockerService.execCmd(apacheContainerName, 
								  constructCmd("tar -xOf "+apacheContainerConfPath+File.separator+apacheConfFile+
										  		" > "+
										  		apacheContainerConfPath+File.separator+apacheConfFile+".plain"));
			log.info("tar extracted");
			
			
			//4. backup
			dockerService.execCmd(apacheContainerName,
								  constructCmd(
								   "cp "+apacheContainerConfPath+File.separator+apacheContainerDefaultConfName+" "
									   +apacheContainerConfPath+File.separator+apacheContainerDefaultConfName+".bak"));
			log.info("backup existing config");
			//5. replace
			dockerService.execCmd(apacheContainerName, 
								  constructCmd(
								  "cp "+apacheContainerConfPath+File.separator+apacheConfFile+".plain "
									   +apacheContainerConfPath+File.separator+apacheContainerDefaultConfName));
			
			log.info("replace config");
			//6. reload apache conf
			dockerService.execCmd(apacheContainerName,
								  constructCmd("service apache2 reload"));
			log.info("reload config");
		}
		catch(DockerServiceException | ApacheConfGenServiceException e){
			throw new MaintenanceServiceException(e);
		}
	}
	
	@Override
	public String removeApp(String userName, String appName) throws MaintenanceServiceException{
		
		//1. check if userApp exist
		if(!findUserApp(userName, appName).isPresent()){
			throw new MaintenanceServiceException("no apps ["+appName+"] found for "+userName);
		}
		
		String containerName = userName+"_"+appName;
		
		try{
			
			//2. locate the container
			log.info("locating docker container [{}]", containerName);
			Optional<Container> containerOpt = dockerService.getContainer(containerName);
			
			if(!containerOpt.isPresent()){
				throw new MaintenanceServiceException("container "+containerName+" not found");
			}
			
			Container container = containerOpt.get();
			
			//3. attempt to remove container's network bridge based on container id and network name (same as containerName)
			Optional<NetworkSettings.Network> networkOpt = dockerService.getContainerNetwork(container.getId(), containerName);
			
			if(networkOpt.isPresent()){
				//use containerName as networkName
				removeContainerNetwork(NETWORK_PREFIX+containerName, container, networkOpt.get());
			}
			else{
				log.info("no network info found, skip network removal");
			}
			
			//4. stop and remove the container
			dockerService.removeApp(container.getId());

			//5. remove from persistence
			//check if properly removed
			if(dockerService.getContainer(containerName).isPresent()){
				throw new MaintenanceServiceException("failed to remove app "+appName+" for user "+userName);
			}
			
			userAppService.delete(UserApp.builder()
										 .userName(userName)
										 .appName(appName)
										 .build());
			
			//6. update apache route config
			updateApacheRouteConfig();
			
			return String.format("%s/%s/", domainUrl, containerName);
			
		}
		catch (DockerServiceException e1) {
			throw new MaintenanceServiceException("Failed starting container ["+containerName+"]",e1);
		}
		catch (UserAppServiceException e) {
			throw new MaintenanceServiceException(e);
		}
	}
	
	private boolean removeContainerNetwork(String networkName, 
										   Container container, 
										   NetworkSettings.Network containerNetwork)
		throws MaintenanceServiceException{
		
		try {
			
			//1. obtain connection id
			Optional<String> connectionIdOpt = dockerService.getConnectionId(networkName, 
																			 container.getId());
			
			//if no longer present return
			if(!connectionIdOpt.isPresent()){
				log.info("network connection no longer present");
				return false;
			}
			else{
				String networkId = connectionIdOpt.get();
				
				try{
					log.info("disconnecting app container from connection bridge");
					dockerService.disconnectConnection(networkId, container.getId());
				}
				catch(Exception e){
					log.warn("failed to disconnect container ["+container.getId()+"] from network ["+networkId+"]");
				}
				
				Optional<String> apacheContainerIdOpt = dockerService.getContainerId("apache2");
				
				if(apacheContainerIdOpt.isPresent()){
					try{
						log.info("disconnecting apache2 container from connection bridge");
						dockerService.disconnectConnection(networkId, apacheContainerIdOpt.get());
					}
					catch(Exception e){
						log.warn("failed to disconnect container ["+container.getId()+"] from network ["+networkId+"]");
					}
				}
				
				dockerService.removeConnection(networkId);
				
				return true;
			}
		} 
		catch (Exception e) {
			throw new MaintenanceServiceException("failed remove container network",e);
		}
	}
	
	private String[] constructCmd(String cmd){
		
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
	
	@Override
	public Optional<UserApp> findUserApp(String userName, String appName) throws MaintenanceServiceException {
		
		try{
			List<service.model.UserApp> userAppList = findUserApp(userName);
			
			return userAppList.stream()
					    	  .filter(obj -> appName.equals(obj.getAppName()))
					    	  .findFirst();
		}
		catch(Exception e){
			throw new MaintenanceServiceException(e);
		}
	}
}
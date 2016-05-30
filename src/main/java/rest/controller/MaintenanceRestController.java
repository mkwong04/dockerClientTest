package rest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import rest.model.InstallUserAppRequest;
import rest.model.InstallUserAppResponse;
import rest.model.UninstallUserAppRequest;
import rest.model.UninstallUserAppResponse;
import rest.model.UserApp;
import rest.model.UserAppListResponse;
import service.MaintenanceService;

@RestController
@Slf4j

public class MaintenanceRestController {
	
	@Autowired
	private MaintenanceService maintenanceService;
	
	@CrossOrigin(origins={"*"}, 
			 methods={RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT},
			 allowedHeaders={"origin", "content-type", "accept", "authorization"})
	@RequestMapping(path="/",method=RequestMethod.GET)
	public void test(){
		log.info("Hello");
	}

	/**
	 * installing user app
	 * @param request
	 * @return
	 */
	@RequestMapping(path="/install", method=RequestMethod.POST, consumes={"application/json"},  produces={"application/json"})
	public ResponseEntity<InstallUserAppResponse> installUserApp(@RequestBody InstallUserAppRequest request){
		log.info("installing user app [{}] for {}",request.getAppName(), request.getUserName());
		
		try{
			//1. check if app already exist
			if(maintenanceService.findUserApp(request.getUserName(), request.getAppName()).isPresent()){
					log.error("App [{}] already installed for user [{}]",request.getAppName(), request.getUserName());
					return new ResponseEntity<InstallUserAppResponse>(InstallUserAppResponse.builder()
																				.status("Error")
																				.errorMsg("App ["+request.getAppName()+"] already installed for user ["+request.getUserName()+"]")
																				.build(), 
																				HttpStatus.BAD_REQUEST);
			}
			
			String userAppUrl = maintenanceService.createApp(request.getUserName(),request.getAppName());
		
			return new ResponseEntity<InstallUserAppResponse>(InstallUserAppResponse.builder()
																		.status("Success")
																		.url(userAppUrl)
																		.build(), 
														HttpStatus.OK);
		}
		catch(Exception e){
			log.error("Failed createApp",e);
			return new ResponseEntity<InstallUserAppResponse>(InstallUserAppResponse.builder()
																		.status("Error")
																		.errorMsg(e.getMessage())
																		.build(), 
																		HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * uninstalling user app
	 * @param request
	 * @return
	 */
	@RequestMapping(path="/uninstall", method=RequestMethod.POST, consumes={"application/json"},  produces={"application/json"})
	public ResponseEntity<UninstallUserAppResponse> uninstallUserApp(@RequestBody UninstallUserAppRequest request){
		log.info("uninstall user app [{}] for {}",request.getAppName(), request.getUserName());
		
		try{
			//1. check if app exist
			if(!maintenanceService.findUserApp(request.getUserName(), request.getAppName()).isPresent()){
					log.error("App [{}] not install for user [{}]",request.getAppName(), request.getUserName());
					return new ResponseEntity<UninstallUserAppResponse>(UninstallUserAppResponse.builder()
																				.status("Error")
																				.errorMsg("App ["+request.getAppName()+"] not install for user ["+request.getUserName()+"]")
																				.build(), 
																		HttpStatus.BAD_REQUEST);
			}
			
			String userAppUrl = maintenanceService.removeApp(request.getUserName(),request.getAppName());
		
			log.info("Successully invalidated {} ", userAppUrl);
			
			return new ResponseEntity<UninstallUserAppResponse>(UninstallUserAppResponse.builder()
																						.status("Success")
																						.build(), 
																HttpStatus.OK);
		}
		catch(Exception e){
			log.error("Failed createApp",e);
			return new ResponseEntity<UninstallUserAppResponse>(UninstallUserAppResponse.builder()
																		.status("Error")
																		.errorMsg(e.getMessage())
																		.build(), 
																		HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * list all user apps
	 * @return
	 */
	@CrossOrigin(origins={"*"}, 
				 methods={RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT},
				 allowedHeaders={"origin", "content-type", "accept", "authorization"})
	@RequestMapping(path="/userApps", 
					method=RequestMethod.GET, 
					produces={"application/json"})
	public ResponseEntity<UserAppListResponse> getAllUserApps(){
		log.info("list all user apps");
		
		try{
			List<service.model.UserApp> userAppList = maintenanceService.listAllUserApp();
			
			List<UserApp> restUserAppList = new ArrayList<>();
			
			for(service.model.UserApp userApp: userAppList){
				restUserAppList.add(UserApp.builder()
										   .id(userApp.getId())
										   .appName(userApp.getAppName())
										   .userName(userApp.getUserName())
										   .build());
			}

			return new ResponseEntity<UserAppListResponse>(UserAppListResponse.builder()
																			  .userAppList(restUserAppList)
																			  .build(),
														   HttpStatus.OK);
		}
		catch(Exception e){
			log.error("Failed listing all user apps");
			return new ResponseEntity<UserAppListResponse> (UserAppListResponse.builder()
																				.build(),
														    HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * find all apps for given user
	 * @param userName
	 * @return
	 */
	@RequestMapping(path="/userApps/user/{userName}", 
				    method=RequestMethod.GET, 
				    produces={"application/json"})
	public ResponseEntity<UserAppListResponse> getUserApps(@PathVariable String userName){
		log.info("list all user apps");
		
		try{
			List<service.model.UserApp> userAppList = maintenanceService.findUserApp(userName);
			
			List<UserApp> restUserAppList = new ArrayList<>();
			
			for(service.model.UserApp userApp: userAppList){
				restUserAppList.add(UserApp.builder()
										   .id(userApp.getId())
										   .appName(userApp.getAppName())
										   .userName(userApp.getUserName())
										   .displayName(userApp.getDisplayName())
										   .slogn(userApp.getSlogan())
										   .description(userApp.getDescription())
										   .features(userApp.getFeatures())
										   .build());
			}

			return new ResponseEntity<UserAppListResponse>(UserAppListResponse.builder()
																			  .userAppList(restUserAppList)
																			  .build(),
														   HttpStatus.OK);
		}
		catch(Exception e){
			log.error("Failed listing all user apps");
			return new ResponseEntity<UserAppListResponse> (UserAppListResponse.builder()
																				.build(),
														    HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

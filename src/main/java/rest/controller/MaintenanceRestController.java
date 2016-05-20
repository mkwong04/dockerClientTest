package rest.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import rest.model.RegisterRequest;
import rest.model.RegisterResponse;
import rest.model.UserApp;
import rest.model.UserAppListResponse;
import service.MaintenanceService;

@RestController
@Slf4j
public class MaintenanceRestController {
	
	@Autowired
	private MaintenanceService maintenanceService;
	
	@RequestMapping("/")
	public void test(){
		log.info("Hello");
	}

	@RequestMapping(path="/signup", method=RequestMethod.POST, consumes={"application/json"},  produces={"application/json"})
	public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest request){
		log.info("register user");
		
		try{		
			String userAppUrl = maintenanceService.createApp(request.getUserName(),request.getAppName());
		
			return new ResponseEntity<RegisterResponse>(RegisterResponse.builder()
																		.status("Success")
																		.url(userAppUrl)
																		.build(), 
														HttpStatus.OK);
		}
		catch(Exception e){
			log.error("Failed createApp",e);
			return new ResponseEntity<RegisterResponse>(RegisterResponse.builder()
																		.status("Error")
																		.errorMsg(e.getMessage())
																		.build(), 
																		HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
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

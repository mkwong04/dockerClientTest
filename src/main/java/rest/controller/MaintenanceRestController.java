package rest.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import rest.model.RegisterRequest;
import rest.model.RegisterResponse;
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
		
		
		//TODO: lookup app's image name 
		String appImageName = "ribbituptest/ribbitup";
		String userAppUrl = maintenanceService.createApp(request.getUserName(),appImageName);
		
		RegisterResponse response = RegisterResponse.builder().url(userAppUrl).build();
		
		return new ResponseEntity<RegisterResponse>(response, HttpStatus.OK);
	}
}

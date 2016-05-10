package service;

import java.util.List;

import service.exception.UserAppServiceException;
import service.model.UserApp;

public interface UserAppService{
	
	UserApp create(UserApp userApp) throws UserAppServiceException;
	
	List<UserApp> findAll() throws UserAppServiceException;
	
	List<UserApp> findByUserName(String userName) throws UserAppServiceException;
}
package service;

import java.util.List;

import service.exception.UserAppServiceException;
import service.model.UserApp;

public interface UserAppService{
	
	/**
	 * 
	 * @param userApp
	 * @return
	 * @throws UserAppServiceException
	 */
	UserApp create(UserApp userApp) throws UserAppServiceException;
	
	/**
	 * 
	 * @param userApp
	 * @throws UserAppServiceException
	 */
	void delete(UserApp userApp) throws UserAppServiceException;
	
	/**
	 * 
	 * @return
	 * @throws UserAppServiceException
	 */
	List<UserApp> findAll() throws UserAppServiceException;
	
	/**
	 * 
	 * @param userName
	 * @return
	 * @throws UserAppServiceException
	 */
	List<UserApp> findByUserName(String userName) throws UserAppServiceException;
}
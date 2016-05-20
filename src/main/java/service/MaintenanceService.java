package service;

import java.util.List;

import service.exception.MaintenanceServiceException;
import service.model.UserApp;

public interface MaintenanceService {

	/**
	 * 
	 * @param userName
	 * @param appImageName
	 * @return
	 * @throws MaintenanceServiceException
	 */
	String createApp(String userName, String appImageName) throws MaintenanceServiceException;

	/**
	 * 
	 * @return
	 * @throws MaintenanceServiceException
	 */
	List<UserApp> listAllUserApp() throws MaintenanceServiceException;
	
	/**
	 * @param userName
	 * @return
	 * @throws MaintenanceServiceException
	 */
	List<UserApp> findUserApp(String userName) throws MaintenanceServiceException;
}
package service;

import java.util.List;
import java.util.Optional;

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
	 * @param userName
	 * @param appImageName
	 * @return
	 * @throws MaintenanceServiceException
	 */
	String removeApp(String userName, String appImageName) throws MaintenanceServiceException;

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
	
	/**
	 * 
	 * @param userName
	 * @param appName
	 * @return
	 * @throws MaintenanceServiceException
	 */
	Optional<UserApp> findUserApp(String userName, String appName) throws MaintenanceServiceException;
}
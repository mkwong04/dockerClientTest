package service;

import service.exception.MaintenanceServiceException;

public interface MaintenanceService {

	/**
	 * 
	 * @param userName
	 * @param appImageName
	 * @return
	 * @throws MaintenanceServiceException
	 */
	String createApp(String userName, String appImageName) throws MaintenanceServiceException;

}
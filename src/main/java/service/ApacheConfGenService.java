package service;

import service.exception.ApacheConfGenServiceException;


public interface ApacheConfGenService{
	
	/**
	 * 
	 * @throws ApacheConfGenServiceException
	 */
	void genConfig() throws ApacheConfGenServiceException;
}
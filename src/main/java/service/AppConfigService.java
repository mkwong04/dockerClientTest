package service;

public interface AppConfigService{
	
	/**
	 * 
	 * @param appName
	 * @return
	 */
	String getImageName(String appName);
	
	/**
	 * 
	 * @param appName
	 * @return
	 */
	String getStartCommand(String appName);
	
	/**
	 * 
	 * @param appName
	 * @return
	 */
	String getStartUrlPattern(String appName);
}
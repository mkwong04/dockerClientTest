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
	
	/**
	 * 
	 * @param appName
	 * @return
	 */
	String getDisplayName(String appName);
	/**
	 * 
	 * @param appName
	 * @return
	 */
	String getSlogan(String appName);
	/**
	 * 
	 * @param appName
	 * @return
	 */
	String getDescription(String appName);
	/**
	 * 
	 * @param appName
	 * @return
	 */
	String getFeatures(String appName);
}
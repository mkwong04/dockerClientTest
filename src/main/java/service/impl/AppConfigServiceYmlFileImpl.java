package service.impl;

import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import service.AppConfigService;

public class AppConfigServiceYmlFileImpl implements AppConfigService{
	
	private Properties userAppConfigProperies;
	
	private static final String PREFIX = "app.";
	private static final String IMAGE_NAME_POST_FIX = ".imageName";
	private static final String START_CMD_POST_FIX = ".startCmd";
	private static final String START_URL_PATTERN_POST_FIX = ".startUrl";
	private static final String DISPLAY_NAME_POST_FIX = ".displayName";
	private static final String SLOGAN_POST_FIX = ".slogan";
	private static final String DESCRIPTION_POST_FIX = ".description";
	private static final String FEATURES_POST_FIX = ".features";
	
	/**
	 * read and populate properties upon instantiated
	 */
	public AppConfigServiceYmlFileImpl(){
		YamlPropertiesFactoryBean facBean = new YamlPropertiesFactoryBean();
		facBean.setResources(new ClassPathResource("userAppConfig.yml"));
		
		userAppConfigProperies = facBean.getObject();
	}

	@Override
	public String getImageName(String appName) {
		return userAppConfigProperies.getProperty(PREFIX+appName+IMAGE_NAME_POST_FIX);
	}

	@Override
	public String getStartCommand(String appName) {
		return userAppConfigProperies.getProperty(PREFIX+appName+START_CMD_POST_FIX);
	}

	@Override
	public String getStartUrlPattern(String appName) {
		return userAppConfigProperies.getProperty(PREFIX+appName+START_URL_PATTERN_POST_FIX);
	}
	
	@Override
	public String getDisplayName(String appName) {
		return userAppConfigProperies.getProperty(PREFIX+appName+DISPLAY_NAME_POST_FIX);
	}
	
	@Override
	public String getSlogan(String appName) {
		return userAppConfigProperies.getProperty(PREFIX+appName+SLOGAN_POST_FIX);
	}
	
	@Override
	public String getDescription(String appName) {
		return userAppConfigProperies.getProperty(PREFIX+appName+DESCRIPTION_POST_FIX);
	}
	
	@Override
	public String getFeatures(String appName) {
		return userAppConfigProperies.getProperty(PREFIX+appName+FEATURES_POST_FIX);
	}
}
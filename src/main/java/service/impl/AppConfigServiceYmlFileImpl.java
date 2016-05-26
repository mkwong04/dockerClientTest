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
	
}
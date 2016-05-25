package app.config;

import java.util.Properties;

import static app.Constant.START_CMD_POSTFIX;
import static app.Constant.START_URL_PATTERN_POSTFIX;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.ApacheConfGenService;
import service.DockerService;
import service.MaintenanceService;
import service.UserAppService;
import service.impl.ApacheConfGenServiceImpl;
import service.impl.DockerServiceDockerJavaImpl;
import service.impl.MaintenanceServiceImpl;
import service.impl.UserAppServiceFileSystemImpl;

@Configuration
@Slf4j
public class AppConfig {

	@Bean
	public MaintenanceService maintenanceService(){
		log.info("maintenanceService");
		return new MaintenanceServiceImpl();
	}
	
	@Bean
	public DockerService dockerService(){
		log.info("dockerService");
		return new DockerServiceDockerJavaImpl();
	}
	
	@Bean
	public UserAppService userAppService(){
		log.info("userAppService");
		return new UserAppServiceFileSystemImpl();
	}
	
	@Bean
	public ApacheConfGenService apacheConfGenService(){
		log.info("apacheConfGenService");
		return new ApacheConfGenServiceImpl();
	}
	
	@Bean(name="appImagesProperties")
	public Properties appImages(){
		Properties appImageProp = new Properties();
		
		appImageProp.put("ui","ribbituptest/ribbitupui");
		appImageProp.put("ui"+START_CMD_POSTFIX,"/home/usr/ribbitup/start");
		appImageProp.put("ui"+START_URL_PATTERN_POSTFIX,"http://%s:9001");
		appImageProp.put("paymentLink","ribbituptest/paymentlink");
		appImageProp.put("paymentLink"+START_CMD_POSTFIX,"/home/usr/paymentlink/paymentlinkTest/start");
		appImageProp.put("paymentLink"+START_URL_PATTERN_POSTFIX,"http://%s:9000");
		
		return appImageProp;
	}
}

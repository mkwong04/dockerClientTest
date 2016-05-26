package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import service.ApacheConfGenService;
import service.AppConfigService;
import service.DockerService;
import service.MaintenanceService;
import service.UserAppService;
import service.impl.ApacheConfGenServiceImpl;
import service.impl.AppConfigServiceYmlFileImpl;
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

	@Bean
	public AppConfigService appConfigService(){
		log.info("Using yml file implementation app config service");
		
		return new AppConfigServiceYmlFileImpl();
	}
}

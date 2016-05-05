package app.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import service.MaintenanceService;
import service.impl.MaintenanceServiceDockerImpl;

@Configuration
@Slf4j
public class AppConfig {

	@Bean
	public MaintenanceService maintenanceService(){
		log.info("maintenanceService");
		return new MaintenanceServiceDockerImpl();
	}
}

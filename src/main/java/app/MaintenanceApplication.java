package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"app.config","rest.controller"})
public class MaintenanceApplication {
	
	public static void main(String[] args) throws Exception{
		SpringApplication.run(MaintenanceApplication.class, args);
	}

}

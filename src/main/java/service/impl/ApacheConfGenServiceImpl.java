package service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import service.ApacheConfGenService;
import service.UserAppService;
import service.exception.ApacheConfGenServiceException;
import service.exception.UserAppServiceException;
import service.model.UserApp;

@Slf4j
public class ApacheConfGenServiceImpl implements ApacheConfGenService{
	
	@Value("${file.dir}")
	private String fileDir;
	
	@Value("${apache.conf.name}")
	private String apacheConfFile;
	
	public static final String TEMPLATE = "<VirtualHost *:80>\n"+
			 							  "    ServerAdmin webmaster@localhost\n"+
			 							  "    DocumentRoot /var/www/html\n"+
			 							  "    ProxyRequests Off\n\n%s\n"+
			 							  "    ErrorLog ${APACHE_LOG_DIR}/error.log\n"+
			 							  "    CustomLog ${APACHE_LOG_DIR}/access.log combined\n"+
			 							  "</VirtualHost>";
	
	
	public static final String PATTERN_1 = "ProxyPass /%s/ %s/";
	public static final String PATTERN_2 = "ProxyHTMLURLMap %s /%s";
	
	public static final String PATTERN_3 = "<Location /%s/>\n"+
											"    ProxyPassReverse /\n"+
											"    ProxyHTMLEnable On\n"+
											"    ProxyHTMLURLMap / /%s/\n"+
											"    RequestHeader unset Accept-Encoding\n"+
											"</Location>";


	@Autowired
	private UserAppService userAppService;

	@Override
	public void genConfig() throws ApacheConfGenServiceException{
		
		try {
			List<UserApp> userAppList = userAppService.findAll();
			
			StringBuilder sb = new StringBuilder();

			for(UserApp userApp: userAppList){
				
				sb.append(String.format(PATTERN_1, userApp.getContainerName(),userApp.getContainerUrl()));
				sb.append(String.format(PATTERN_2, userApp.getContainerUrl(), userApp.getContainerName()));
				sb.append(String.format(PATTERN_3, userApp.getContainerName(), userApp.getContainerName()));
			}
			
			String finalConfigStr = String.format(TEMPLATE, sb.toString());
			
			log.info(finalConfigStr);
			
			File dataFile = new File(fileDir+File.pathSeparator+apacheConfFile);
			
			try(FileWriter fw = new FileWriter(dataFile, true);){
				fw.write(finalConfigStr);
				
				fw.flush();
			}
			catch(IOException e){
				log.error("Failed to write",e);
			}
		} 
		catch (UserAppServiceException e) {
			throw new ApacheConfGenServiceException(e);
		}
		
	}
} 
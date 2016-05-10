package service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import service.UserAppService;
import service.exception.UserAppServiceException;
import service.model.UserApp;

@Slf4j
public class UserAppServiceFileSystemImpl implements UserAppService{
	
	@Value("${file.dir}")
	private String fileDir;
	
	@Value("${file.name}")
	private String userAppDataFile;

	@Override
	public UserApp create(UserApp userApp) throws UserAppServiceException{
		
		File fDir = new File(fileDir);
		
		//if not exist
		if(!fDir.isDirectory()){
			//create
			if(!fDir.mkdir()){
				throw new UserAppServiceException("failed to create dir");
			}
		}
		
		File dataFile = new File(fileDir+File.pathSeparator+userAppDataFile);
		
		try(FileWriter fw = new FileWriter(dataFile, true);){
			fw.write(String.format("%s,%s,%s,%s,%s\n", 
								   userApp.getId(),
								   userApp.getUserName(),
								   userApp.getAppName(),
								   userApp.getContainerName(),
								   userApp.getContainerUrl()));
			
			fw.flush();
		}
		catch(IOException e){
			log.error("error writing",e);
		}
		
		return userApp;
	}

	@Override
	public List<UserApp> findAll() {
		List<UserApp> results = new ArrayList<>();
		
		File dataFile = new File(fileDir+File.pathSeparator+userAppDataFile);
		
		try(FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr)){
			
			String record;
			while((record = br.readLine())!=null){
				String[] data = record.split(",");

				if(data.length==5){
					results.add(UserApp.builder()
									   .id(data[0])
									   .userName(data[1])
									   .appName(data[2])
									   .containerName(data[3])
									   .containerUrl(data[4])
									   .build());
				}
			}
			
		}
		catch(IOException e){
			
		}
		
		return results;
	}

	@Override
	public List<UserApp> findByUserName(String userName) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
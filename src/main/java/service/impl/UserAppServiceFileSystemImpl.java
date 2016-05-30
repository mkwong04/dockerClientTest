package service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import service.UserAppService;
import service.exception.UserAppServiceException;
import service.model.UserApp;

@Slf4j
public class UserAppServiceFileSystemImpl implements UserAppService{
	
	private static final String DELIMINATOR = "\u0009";
	private static final String USER_APP_PATTERN = 	"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"%s"+DELIMINATOR+
													"\n";
	
	private int FIELD_PER_ROW = 9;
	
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
		
		File dataFile = new File(fileDir+File.separator+userAppDataFile);
		
		try(FileWriter fw = new FileWriter(dataFile, true);){
			fw.write(String.format(USER_APP_PATTERN, 
								   userApp.getId(),
								   userApp.getUserName(),
								   userApp.getAppName(),
								   userApp.getContainerName(),
								   userApp.getContainerUrl(),
								   userApp.getDisplayName(),
								   userApp.getSlogan(),
								   userApp.getDescription(),
								   userApp.getFeatures()));
			
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
		
		File dataFile = new File(fileDir+File.separator+userAppDataFile);
		
		try(FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr)){
			
			String record;
			while((record = br.readLine())!=null){
				String[] data = record.split(DELIMINATOR);

				if(data.length==FIELD_PER_ROW){
					results.add(buildUserApp(data));
				}
			}
			
		}
		catch(IOException e){
			
		}
		
		return results;
	}

	@Override
	public List<UserApp> findByUserName(String userName) {
		List<UserApp> results = new ArrayList<>();
		
		File dataFile = new File(fileDir+File.separator+userAppDataFile);
		
		try(FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr)){
			
			String record;
			while((record = br.readLine())!=null){
				String[] data = record.split(DELIMINATOR);

				if(data.length==FIELD_PER_ROW && 
				   data[1]!=null && data[1].equals(userName)){
					results.add(buildUserApp(data));
				}
			}
			
		}
		catch(IOException e){
			
		}
		
		return results;
	}
	
	private UserApp buildUserApp(String[] data){
		return UserApp.builder()
					  .id(data[0])
					  .userName(data[1])
					  .appName(data[2])
					  .containerName(data[3])
					  .containerUrl(data[4])
					  .displayName(data[5])
					  .slogan(data[6])
					  .description(data[7])
					  .features(data[8])
					  .build();
	}

	@Override
	public void delete(UserApp userApp) throws UserAppServiceException {
		
		File dataFile = new File(fileDir+File.separator+userAppDataFile);
		
		try(RandomAccessFile raf = new RandomAccessFile(dataFile,"rwd");){
			
			boolean recordFound = false;
			String record; 
			long filePointerOffset = 0;
			long lengthOfRecord;
					
			//search line by line read for record
			while ((record = raf.readLine() )!= null) {
				String[] data = record.split(DELIMINATOR);

				//if record found
				if(data.length==FIELD_PER_ROW && 
				   data[1]!=null && data[1].equals(userApp.getUserName()) &&
				   data[2]!=null && data[2].equals(userApp.getAppName())){
					recordFound = true;
					break;
				}
				
				//keep the file pointer position
				filePointerOffset = raf.getFilePointer();
			}
			
			if(!recordFound){
				throw new UserAppServiceException("no record found for "+userApp.getUserName()+":"+userApp.getAppName());
			}
			
			//last readline will shift the file pointer offset, so length of record = current file pointer offset - last record read offset
			lengthOfRecord = raf.getFilePointer() - filePointerOffset;
			
			/**** moving the records up block by block ****/
			
			//read buffer of block size of 8k byte
			byte[] buffer = new byte[8192];
			int byteRead;
			long lastReadFilePointerOffset;
			
			//read into buffer by block size of max 8k
			while((byteRead = raf.read(buffer))> -1){
				
				//note position after read
				lastReadFilePointerOffset = raf.getFilePointer();
				
				//move back to position before the buffer read and minus the length of record
				raf.seek( lastReadFilePointerOffset - byteRead - lengthOfRecord);
				
				//overwrite the data at file position with buffer read content
				raf.write(buffer, 0, byteRead);
				
				//move to last read file pointer position
				raf.seek(lastReadFilePointerOffset);
			}
			
			//truncate the last data (residual of data block after the record moved up)
			raf.setLength(raf.length()-lengthOfRecord);
		}
		catch(IOException e){
			throw new UserAppServiceException("Failed deleting user App for "+userApp.getUserName()+":"+userApp.getAppName());
		}
	}
}
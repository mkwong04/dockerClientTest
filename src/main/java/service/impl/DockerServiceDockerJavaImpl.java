package service.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import service.DockerService;
import service.exception.DockerServiceException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.NetworkSettings.Network;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;

@Slf4j
public class DockerServiceDockerJavaImpl implements DockerService{
	
	@Value("${docker.url}")
	private String dockerUrl;
	
	@Value("${file.dir}")
	private String fileDir;
	
	@Value("${apache.conf.name}")
	private String apacheConfFile;
	
	@Override
	public String createApp(String containerName, 
							String appImageName, 
							String containerListenUrlPattern,
							String... startCmd) 
		throws DockerServiceException{
		String userAppUrl = null;
		
		log.info("dockerUrl : {}",dockerUrl);
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
		
			log.info("creating container from image : {}",appImageName);
			
			CreateContainerResponse container = dockerClient.createContainerCmd(appImageName)
//														    .withCmd(startCmd)
														    .withName(containerName)
														    .exec();
			
			log.info("starting container : {}",container.getId());
			dockerClient.startContainerCmd(container.getId()).exec();
			
			execCmd(containerName, startCmd);
			
			log.info("inspecting container");
			InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
			
			Network network = inspectResponse.getNetworkSettings().getNetworks().get("bridge");
			
			userAppUrl = String.format(containerListenUrlPattern, network.getIpAddress());
			
			log.info("redirect target url :{}",userAppUrl);
			
		}
		catch(IOException e){
			log.error("Failed create docker client",e);
		}
		
		return userAppUrl;
	}

	@Override
	public void copyFile(String sourceContainerName, 
						 String sourcePath, 
						 String targetContainerName, 
						 String targetPath) {
		
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();
			BufferedInputStream bis = new BufferedInputStream(dockerClient.copyArchiveFromContainerCmd(sourceContainerName, sourcePath).exec());){
			
			Path outputPath = Paths.get(fileDir+File.separator+apacheConfFile);
			//write to host system 
			Files.copy(bis, outputPath, StandardCopyOption.REPLACE_EXISTING);			
			
			dockerClient.copyArchiveToContainerCmd(targetContainerName)
						.withRemotePath(targetPath)
						.withHostResource(fileDir+File.separator+apacheConfFile)
						.exec();
		}
		catch(IOException e){
			log.error("Failed create docker client",e);
		}
	}
	
	@Override
	public void execCmd(String containerName, String... cmd) throws DockerServiceException{
		try(DockerClient dockerClient = DockerClientBuilder.getInstance(createConfig()).build();){
			ExecCreateCmdResponse response = dockerClient.execCreateCmd(containerName)
														 .withCmd(cmd)
														 .withAttachStdin(false)
														 .withAttachStdout(true)
														 .withAttachStderr(true)
														 .exec();
			
			log.info("exec id :{}",response.getId());
			
//			if(!dockerClient.execStartCmd(response.getId())
//						 .withDetach(false)
//						 .withTty(false)
//						 .exec(new ExecStartResultCallback())
//						 .awaitCompletion(1, TimeUnit.MINUTES)){
//				throw new DockerServiceException("time out while execute command "+cmd);
//			}
		}
		catch(IOException e){
			throw new DockerServiceException(e);
		}
	}
	
	
	private DockerClientConfig createConfig(){
		return DockerClientConfig.createDefaultConfigBuilder()
								  .withDockerHost(dockerUrl)
								  .withDockerTlsVerify(false)
								  .withApiVersion("1.21")
								  .build();
	}

	
}
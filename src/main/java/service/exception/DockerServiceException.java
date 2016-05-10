package service.exception;

public class DockerServiceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DockerServiceException(){
		super();
	}
	
	/**
	 * 
	 * @param msg
	 */
	public DockerServiceException(String msg){
		super(msg);
	}
	
	/**
	 * 
	 * @param t
	 */
	public DockerServiceException(Throwable t){
		super(t);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public DockerServiceException(String msg, Throwable t){
		super(msg,t);
	}
}
package service.exception;

public class UserAppServiceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public UserAppServiceException(){
		super();
	}
	
	/**
	 * 
	 * @param msg
	 */
	public UserAppServiceException(String msg){
		super(msg);
	}
	
	/**
	 * 
	 * @param t
	 */
	public UserAppServiceException(Throwable t){
		super(t);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public UserAppServiceException(String msg, Throwable t){
		super(msg,t);
	}
}
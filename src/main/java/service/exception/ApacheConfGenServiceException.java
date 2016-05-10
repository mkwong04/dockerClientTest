package service.exception;

public class ApacheConfGenServiceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ApacheConfGenServiceException(){
		super();
	}
	
	/**
	 * 
	 * @param msg
	 */
	public ApacheConfGenServiceException(String msg){
		super(msg);
	}
	
	/**
	 * 
	 * @param t
	 */
	public ApacheConfGenServiceException(Throwable t){
		super(t);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public ApacheConfGenServiceException(String msg, Throwable t){
		super(msg,t);
	}
}
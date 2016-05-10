package service.exception;

public class MaintenanceServiceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MaintenanceServiceException(){
		super();
	}
	
	/**
	 * 
	 * @param msg
	 */
	public MaintenanceServiceException(String msg){
		super(msg);
	}
	
	/**
	 * 
	 * @param t
	 */
	public MaintenanceServiceException(Throwable t){
		super(t);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public MaintenanceServiceException(String msg, Throwable t){
		super(msg,t);
	}
}
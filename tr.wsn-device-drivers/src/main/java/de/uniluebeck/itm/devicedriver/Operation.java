package de.uniluebeck.itm.devicedriver;


/**
 * A device operation.
 * 
 * @author Malte Legenhausen
 *
 * @param <T> The return type of the operation.
 */
public interface Operation<T> {

	/**
	 * Method that is called when the operation has to be executed.
	 * 
	 * @param monitor The monitor for this operation.
	 */
	void run(Monitor monitor);
	
	/**
	 * Returns the result of the run operation.
	 * 
	 * @return The result of the operation.
	 */
	T getResult();
	
	/**
	 * Cancel the operation.
	 */
	void cancel();
	
	/**
	 * Returns if the operation will be canceled.
	 * 
	 * @return true if the operation will be canceled else false.
	 */
	boolean isCanceled();
}
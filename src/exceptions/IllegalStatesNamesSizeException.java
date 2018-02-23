/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package exceptions;

public class IllegalStatesNamesSizeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2395251804073036958L;

	public IllegalStatesNamesSizeException(String message) {
		super(message);
	}

	public IllegalStatesNamesSizeException() {
		super();
	}
}

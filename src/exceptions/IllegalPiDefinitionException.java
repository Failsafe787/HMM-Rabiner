/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package exceptions;

public class IllegalPiDefinitionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1304749095110131193L;

	public IllegalPiDefinitionException(String message) {
		super(message);
	}

	public IllegalPiDefinitionException() {
		super();
	}
}

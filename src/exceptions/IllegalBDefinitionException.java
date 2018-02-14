/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package exceptions;

public class IllegalBDefinitionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5383136957062027756L;

	public IllegalBDefinitionException(String message) {
		super(message);
	}

	public IllegalBDefinitionException() {
		super();
	}
}

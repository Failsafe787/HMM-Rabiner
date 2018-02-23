/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package exceptions;

public class IllegalADefinitionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8786973103220109249L;

	public IllegalADefinitionException(String message) {
		super(message);
	}

	public IllegalADefinitionException() {
		super();
	}

}

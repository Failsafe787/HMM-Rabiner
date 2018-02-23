/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package exceptions;

public class IllegalCurvesNumberException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5857543910051957540L;

	public IllegalCurvesNumberException(String message) {
		super(message);
	}

	public IllegalCurvesNumberException() {
		super();
	}

}

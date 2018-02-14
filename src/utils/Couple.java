/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utils;

// (C) 2017 JavaTuples

public class Couple {

	private int x;
	private double value;

	public Couple(int x, double value) {
		this.x = x;
		this.value = value;
	}

	// getter X
	public int getX() {
		return x;
	}

	// setter Value
	public void setValue(double value) {
		this.value = value;
	}

	// getter Value
	public double getValue() {
		return value;
	}

	// toString, customizable as you please
	@Override
	public String toString() {
		return "[" + x + ", " + value + "]";
	}

}
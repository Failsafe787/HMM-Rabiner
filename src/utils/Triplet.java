/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1
 */

package utils;

// (C) 2017 JavaTuples

public class Triplet extends Couple { // Triplet is an Extension of a Couple object (with an extra field)

	private int y;

	public Triplet(int x, int y, double value) {
		super(x, value);
		this.y = y;
	}

	// getter Y
	public int getY() {
		return y;
	}

	// toString, customizable as you please
	@Override
	public String toString() {
		return "[" + super.getX() + ", " + y + ", " + super.getValue() + "]";
	}

}
/*
 * Released under MIT License (Expat)
 * @author Luca Banzato
 * @version 0.1.8
 */

package utils;

public class Observation {

	public int time;
	public double value;

	public Observation() {
		time = 0;
		value = 0.0;
	}

	public Observation(int time, double value) {
		this.time = time;
		this.value = value;
	}

	@Override
	public String toString() {
		return time + " " + value;
	}

}